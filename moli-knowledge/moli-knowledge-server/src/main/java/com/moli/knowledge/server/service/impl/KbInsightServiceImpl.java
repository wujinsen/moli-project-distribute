package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentTag;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbDocumentTagMapper;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbInsightService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KbInsightServiceImpl implements KbInsightService {

    /** 匹配 [[目标]] 或 [[目标|显示文本]]，取第一段为目标。 */
    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentTagMapper kbDocumentTagMapper;
    @Resource
    private KbCategoryMapper kbCategoryMapper;
    @Resource
    private KbRelationMapper kbRelationMapper;
    @Resource
    private KbLintIssueMapper kbLintIssueMapper;
    @Resource
    private KbAclService kbAclService;

    /** 图谱默认最多返回节点数（按度数降序保留），避免一次性把全库推给前端。 */
    private static final int GRAPH_DEFAULT_MAX_NODES = 300;
    private static final int GRAPH_MAX_NODES_CAP = 2000;
    /** summary 模式默认返回的 Top 枢纽数量。 */
    private static final int GRAPH_SUMMARY_TOP = 50;
    /** ego 子图默认节点上限与最大跳数。 */
    private static final int EGO_DEFAULT_MAX_NODES = 200;
    private static final int EGO_MAX_DEPTH = 3;

    private static final String MODE_SUMMARY = "summary";
    private static final String MODE_EGO = "ego";
    private static final String MODE_FULL = "full";

    @Override
    public GraphVo graph(Long spaceId, String mode, Integer maxNodes, Integer minDeg) {
        assertSpaceReadable(spaceId);
        List<Long> scope = resolveScopeSpaceIds(spaceId);
        if (scope.isEmpty()) {
            return emptyGraph(MODE_FULL, "relation");
        }

        boolean summary = MODE_SUMMARY.equalsIgnoreCase(mode);

        // 边来源：优先 kb_relation（已落库，不扫正文）；为空时回退运行时解析
        List<GraphVo.Link> allLinks = linksFromRelation(scope);
        String source = "relation";
        Map<Long, Integer> degree;
        if (allLinks.isEmpty()) {
            // 回退：小库 / 尚未同步关系。沿用运行时解析（含正文扫描，仅小库可接受）
            Ctx ctx = build(spaceId);
            allLinks = ctx.links;
            degree = ctx.degree;
            source = "runtime";
        } else {
            degree = degreeOf(allLinks);
        }

        int effMax = summary
                ? (maxNodes == null || maxNodes <= 0 ? GRAPH_SUMMARY_TOP : Math.min(maxNodes, GRAPH_MAX_NODES_CAP))
                : (maxNodes == null || maxNodes <= 0 ? GRAPH_DEFAULT_MAX_NODES : Math.min(maxNodes, GRAPH_MAX_NODES_CAP));
        int effMinDeg = minDeg == null ? 0 : Math.max(0, minDeg);

        int totalNodesInScope = countDocs(scope);
        int totalLinks = allLinks.size();

        // 候选节点 = 出现在边里的节点（孤儿对图谱无意义），按度数降序、minDeg 过滤后截断
        List<Long> ranked = degree.entrySet().stream()
                .filter(e -> e.getValue() >= effMinDeg)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        boolean truncated = ranked.size() > effMax;
        List<Long> keepIds = truncated ? ranked.subList(0, effMax) : ranked;
        Set<Long> keepSet = new HashSet<>(keepIds);

        GraphVo vo = new GraphVo();
        GraphVo.Meta meta = vo.getMeta();
        meta.setTotalNodes(totalNodesInScope);
        meta.setTotalLinks(totalLinks);
        meta.setSource(source);
        meta.setMode(summary ? MODE_SUMMARY : MODE_FULL);

        // summary 模式只回 Top 枢纽节点 + 它们之间的边，供前端先画概览
        Map<Long, KbDocument> nodeInfo = loadLightNodes(keepSet);
        Map<Long, String> categoryName = loadCategoryNames();
        for (Long id : keepIds) {
            KbDocument d = nodeInfo.get(id);
            if (d == null) {
                continue;
            }
            vo.getNodes().add(buildNode(d, categoryName, degree.getOrDefault(id, 0)));
        }

        for (GraphVo.Link l : allLinks) {
            if (keepSet.contains(Long.valueOf(l.getSource())) && keepSet.contains(Long.valueOf(l.getTarget()))) {
                vo.getLinks().add(l);
            }
        }

        meta.setReturnedNodes(vo.getNodes().size());
        meta.setReturnedLinks(vo.getLinks().size());
        meta.setTruncated(truncated);
        return vo;
    }

    @Override
    public GraphVo ego(Long spaceId, Long docId, Integer depth, Integer maxNodes) {
        if (docId == null) {
            throw new com.moli.common.exception.BaseException("docId 不能为空");
        }
        assertSpaceReadable(spaceId);
        List<Long> scope = resolveScopeSpaceIds(spaceId);
        if (scope.isEmpty()) {
            return emptyGraph(MODE_EGO, "relation");
        }
        int effDepth = depth == null ? 1 : Math.min(Math.max(1, depth), EGO_MAX_DEPTH);
        int effMax = maxNodes == null || maxNodes <= 0 ? EGO_DEFAULT_MAX_NODES : Math.min(maxNodes, GRAPH_MAX_NODES_CAP);

        // 逐层向 kb_relation 查邻居（避免一次性加载全图）
        Set<Long> visited = new LinkedHashSet<>();
        visited.add(docId);
        Set<Long> frontier = new HashSet<>();
        frontier.add(docId);
        for (int d = 0; d < effDepth && visited.size() < effMax && !frontier.isEmpty(); d++) {
            Set<Long> next = neighborsOf(scope, frontier);
            next.removeAll(visited);
            for (Long id : next) {
                if (visited.size() >= effMax) {
                    break;
                }
                visited.add(id);
            }
            frontier = next;
        }

        List<GraphVo.Link> edges = edgesAmong(scope, visited);
        Map<Long, Integer> degree = degreeOf(edges);

        GraphVo vo = new GraphVo();
        Map<Long, KbDocument> nodeInfo = loadLightNodes(visited);
        Map<Long, String> categoryName = loadCategoryNames();
        for (Long id : visited) {
            KbDocument doc = nodeInfo.get(id);
            if (doc == null) {
                continue;
            }
            vo.getNodes().add(buildNode(doc, categoryName, degree.getOrDefault(id, 0)));
        }
        vo.setLinks(edges);

        GraphVo.Meta meta = vo.getMeta();
        meta.setMode(MODE_EGO);
        meta.setSource("relation");
        meta.setTotalNodes(vo.getNodes().size());
        meta.setTotalLinks(edges.size());
        meta.setReturnedNodes(vo.getNodes().size());
        meta.setReturnedLinks(edges.size());
        meta.setTruncated(visited.size() >= effMax);
        return vo;
    }

    // ------------------------------------------------------------------
    // 图谱快路径辅助（只读 kb_relation + 轻量节点，绝不扫正文）
    // ------------------------------------------------------------------

    /** 作用域内已解析边（resolved=1），不限制两端是否在某子集。 */
    private List<GraphVo.Link> linksFromRelation(List<Long> scope) {
        LambdaQueryWrapper<KbRelation> w = new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 1)
                .isNotNull(KbRelation::getTargetDocId)
                .in(KbRelation::getSpaceId, scope)
                .select(KbRelation::getSourceDocId, KbRelation::getTargetDocId, KbRelation::getRelationType);
        List<GraphVo.Link> links = new ArrayList<>();
        for (KbRelation r : kbRelationMapper.selectList(w)) {
            links.add(new GraphVo.Link(String.valueOf(r.getSourceDocId()),
                    String.valueOf(r.getTargetDocId()), r.getRelationType()));
        }
        return links;
    }

    /** ego BFS：查 frontier 节点的一跳邻居（source/target 双向）。 */
    private Set<Long> neighborsOf(List<Long> scope, Set<Long> frontier) {
        Set<Long> result = new HashSet<>();
        if (frontier.isEmpty()) {
            return result;
        }
        List<KbRelation> rows = kbRelationMapper.selectList(new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 1)
                .isNotNull(KbRelation::getTargetDocId)
                .in(KbRelation::getSpaceId, scope)
                .and(q -> q.in(KbRelation::getSourceDocId, frontier).or().in(KbRelation::getTargetDocId, frontier))
                .select(KbRelation::getSourceDocId, KbRelation::getTargetDocId));
        for (KbRelation r : rows) {
            result.add(r.getSourceDocId());
            result.add(r.getTargetDocId());
        }
        return result;
    }

    /** 两端都在 ids 集合内的边。 */
    private List<GraphVo.Link> edgesAmong(List<Long> scope, Set<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<KbRelation> rows = kbRelationMapper.selectList(new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 1)
                .in(KbRelation::getSourceDocId, ids)
                .in(KbRelation::getTargetDocId, ids)
                .select(KbRelation::getSourceDocId, KbRelation::getTargetDocId, KbRelation::getRelationType));
        List<GraphVo.Link> links = new ArrayList<>();
        for (KbRelation r : rows) {
            links.add(new GraphVo.Link(String.valueOf(r.getSourceDocId()),
                    String.valueOf(r.getTargetDocId()), r.getRelationType()));
        }
        return links;
    }

    private Map<Long, Integer> degreeOf(List<GraphVo.Link> links) {
        Map<Long, Integer> degree = new HashMap<>();
        for (GraphVo.Link l : links) {
            degree.merge(Long.valueOf(l.getSource()), 1, Integer::sum);
            degree.merge(Long.valueOf(l.getTarget()), 1, Integer::sum);
        }
        return degree;
    }

    /** 仅取图谱所需字段，绝不 select content（longtext）。 */
    private Map<Long, KbDocument> loadLightNodes(Set<Long> ids) {
        Map<Long, KbDocument> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        List<KbDocument> docs = kbDocumentMapper.selectList(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE)
                .in(KbDocument::getId, ids)
                .select(KbDocument::getId, KbDocument::getTitle, KbDocument::getKbType,
                        KbDocument::getCategoryId, KbDocument::getStatus));
        for (KbDocument d : docs) {
            map.put(d.getId(), d);
        }
        return map;
    }

    private Map<Long, String> loadCategoryNames() {
        Map<Long, String> map = new HashMap<>();
        for (KbCategory c : kbCategoryMapper.selectList(new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE)
                .select(KbCategory::getId, KbCategory::getCategoryName))) {
            map.put(c.getId(), c.getCategoryName());
        }
        return map;
    }

    /** 节点 type：优先 kbType（与浏览分组一致），否则分类名，再否则状态标签。 */
    private GraphVo.Node buildNode(KbDocument d, Map<Long, String> categoryName, int deg) {
        GraphVo.Node node = new GraphVo.Node();
        node.setId(String.valueOf(d.getId()));
        node.setTitle(d.getTitle());
        node.setDeg(deg);
        if (StringUtils.isNotBlank(d.getKbType())) {
            node.setType(d.getKbType());
        } else if (d.getCategoryId() != null && StringUtils.isNotBlank(categoryName.get(d.getCategoryId()))) {
            node.setType(categoryName.get(d.getCategoryId()));
        } else {
            node.setType(statusLabel(d.getStatus()));
        }
        return node;
    }

    private String statusLabel(Integer status) {
        if (status != null) {
            for (DocumentStatus s : DocumentStatus.values()) {
                if (s.getCode() == status) {
                    return s.getLabel();
                }
            }
        }
        return "未分类";
    }

    private List<Long> resolveScopeSpaceIds(Long spaceId) {
        if (spaceId != null) {
            return Collections.singletonList(spaceId);
        }
        return kbAclService.accessibleSpaceIds();
    }

    private int countDocs(List<Long> scope) {
        Integer c = kbDocumentMapper.selectCount(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE)
                .in(KbDocument::getSpaceId, scope));
        return c == null ? 0 : c;
    }

    private GraphVo emptyGraph(String mode, String source) {
        GraphVo vo = new GraphVo();
        vo.getMeta().setMode(mode);
        vo.getMeta().setSource(source);
        return vo;
    }

    @Override
    public LintVo lint(Long spaceId) {
        assertSpaceReadable(spaceId);
        Ctx ctx = build(spaceId);
        LintVo vo = new LintVo();
        vo.setBroken(ctx.broken);
        for (KbDocument d : ctx.docs) {
            Set<Long> in = ctx.inbound.get(d.getId());
            if (in == null || in.isEmpty()) {
                vo.getOrphans().add(new LintVo.Ref(String.valueOf(d.getId()), d.getTitle()));
            }
            if (StringUtils.isBlank(d.getSummary())) {
                vo.getNoSummary().add(new LintVo.Ref(String.valueOf(d.getId()), d.getTitle()));
            }
        }
        Map<String, Integer> counts = vo.getCounts();
        counts.put("pages", ctx.docs.size());
        counts.put("broken", ctx.broken.size());
        counts.put("orphans", vo.getOrphans().size());
        counts.put("noSummary", vo.getNoSummary().size());
        return vo;
    }

    @Override
    public LintVo scan(Long spaceId) {
        kbAclService.assertCanLintScan(spaceId);
        LintVo vo = lint(spaceId);
        Date now = new Date();

        // 清掉本空间「待处理」旧项后重建（保留已忽略/已修复的人工决定）
        LambdaQueryWrapper<KbLintIssue> del = new LambdaQueryWrapper<KbLintIssue>()
                .eq(KbLintIssue::getStatus, 0);
        if (spaceId != null) {
            del.eq(KbLintIssue::getSpaceId, spaceId);
        }
        kbLintIssueMapper.delete(del);

        for (LintVo.Broken b : vo.getBroken()) {
            insertIssue(spaceId, parseLong(b.getPage()), "broken_link",
                    b.getTitle() + " -> [[" + b.getTarget() + "]]", now);
        }
        for (LintVo.Ref r : vo.getOrphans()) {
            insertIssue(spaceId, parseLong(r.getSlug()), "orphan", r.getTitle(), now);
        }
        for (LintVo.Ref r : vo.getNoSummary()) {
            insertIssue(spaceId, parseLong(r.getSlug()), "no_summary", r.getTitle(), now);
        }
        return vo;
    }

    @Override
    public List<KbLintIssue> issues(Long spaceId, Integer status) {
        assertSpaceReadable(spaceId);
        LambdaQueryWrapper<KbLintIssue> w = new LambdaQueryWrapper<>();
        if (spaceId != null) {
            w.eq(KbLintIssue::getSpaceId, spaceId);
        }
        if (status != null) {
            w.eq(KbLintIssue::getStatus, status);
        }
        w.orderByDesc(KbLintIssue::getScanTime);
        return kbLintIssueMapper.selectList(w);
    }

    @Override
    public void updateIssueStatus(Long id, Integer status) {
        KbLintIssue issue = kbLintIssueMapper.selectById(id);
        if (issue != null) {
            if (issue.getSpaceId() != null) {
                kbAclService.assertCanEdit(issue.getSpaceId());
            } else if (!kbAclService.isAdmin()) {
                throw new com.moli.common.exception.BaseException("无权处理该体检问题");
            }
            issue.setStatus(status);
            issue.setUpdateTime(new Date());
            kbLintIssueMapper.updateById(issue);
        }
    }

    private void insertIssue(Long spaceId, Long docId, String type, String detail, Date now) {
        KbLintIssue issue = new KbLintIssue();
        issue.setId(IdGenerator.getId());
        issue.setSpaceId(spaceId);
        issue.setDocumentId(docId);
        issue.setIssueType(type);
        issue.setDetail(detail == null ? "" : detail.substring(0, Math.min(500, detail.length())));
        issue.setStatus(0);
        issue.setScanTime(now);
        issue.setCreateTime(now);
        issue.setUpdateTime(now);
        kbLintIssueMapper.insert(issue);
    }

    private Long parseLong(String s) {
        try {
            return s == null ? null : Long.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // 链接计算：graph / lint 共用
    // ------------------------------------------------------------------

    private Ctx build(Long spaceId) {
        Ctx ctx = new Ctx();
        ctx.docs = loadDocs(spaceId);

        // 分类名（用于节点分组着色）
        List<KbCategory> categories = kbCategoryMapper.selectList(new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE));
        for (KbCategory c : categories) {
            ctx.categoryName.put(c.getId(), c.getCategoryName());
        }

        // 标题 / slug → 文档ID（与 lint.py、KbWikiAiReviseServiceImpl 一致：标题 + 全路径 slug + 末段）
        Set<Long> idSet = new HashSet<>();
        Map<String, Long> titleIndex = new HashMap<>();
        Map<String, Long> slugIndex = new HashMap<>();
        for (KbDocument d : ctx.docs) {
            idSet.add(d.getId());
            if (StringUtils.isNotBlank(d.getTitle())) {
                titleIndex.putIfAbsent(d.getTitle().trim().toLowerCase(Locale.ROOT), d.getId());
            }
            if (StringUtils.isNotBlank(d.getSlug())) {
                String slug = d.getSlug().trim().toLowerCase(Locale.ROOT);
                slugIndex.putIfAbsent(slug, d.getId());
                int slash = slug.lastIndexOf('/');
                if (slash >= 0) {
                    slugIndex.putIfAbsent(slug.substring(slash + 1), d.getId());
                }
            }
        }

        // 1) 正文 [[标题]] 引用
        for (KbDocument d : ctx.docs) {
            String body = d.getContent() == null ? "" : d.getContent();
            Matcher m = WIKILINK.matcher(body);
            Set<Long> seen = new HashSet<>();
            while (m.find()) {
                String target = m.group(1).split("\\|")[0].trim();
                if (target.isEmpty()) {
                    continue;
                }
                Long tid = resolveWikilinkTarget(target, titleIndex, slugIndex);
                if (tid == null) {
                    ctx.broken.add(new LintVo.Broken(String.valueOf(d.getId()), d.getTitle(), target));
                } else if (!tid.equals(d.getId()) && seen.add(tid)) {
                    addLink(ctx, d.getId(), tid, "links_to");
                    ctx.inbound.computeIfAbsent(tid, k -> new HashSet<>()).add(d.getId());
                }
            }
        }

        // 2) 同标签关联（无向，去重 pair）
        Map<Long, Set<Long>> tagDocs = new HashMap<>();
        List<KbDocumentTag> relations = kbDocumentTagMapper.selectList(new LambdaQueryWrapper<>());
        for (KbDocumentTag r : relations) {
            if (idSet.contains(r.getDocumentId())) {
                tagDocs.computeIfAbsent(r.getTagId(), k -> new HashSet<>()).add(r.getDocumentId());
            }
        }
        Set<String> pairSeen = new HashSet<>();
        for (Set<Long> group : tagDocs.values()) {
            List<Long> list = new ArrayList<>(group);
            Collections.sort(list);
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    Long a = list.get(i);
                    Long b = list.get(j);
                    if (pairSeen.add(a + "-" + b)) {
                        addLink(ctx, a, b, "same_tag");
                        ctx.inbound.computeIfAbsent(a, k -> new HashSet<>()).add(b);
                        ctx.inbound.computeIfAbsent(b, k -> new HashSet<>()).add(a);
                    }
                }
            }
        }
        return ctx;
    }

    /**
     * 解析 [[target]]：先标题，再全路径 slug，再 slug 末段（如 [[本地启动指南]] → guides/本地启动指南）。
     * 与 {@link KbWikiAiReviseServiceImpl}、{@code lint.py resolve()} 对齐。
     */
    static Long resolveWikilinkTarget(String target, Map<String, Long> titleIndex, Map<String, Long> slugIndex) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        String t = target.trim().toLowerCase(Locale.ROOT);
        Long byTitle = titleIndex.get(t);
        if (byTitle != null) {
            return byTitle;
        }
        Long bySlug = slugIndex.get(t);
        if (bySlug != null) {
            return bySlug;
        }
        int slash = t.lastIndexOf('/');
        if (slash >= 0) {
            Long byTail = slugIndex.get(t.substring(slash + 1));
            if (byTail != null) {
                return byTail;
            }
        }
        for (Map.Entry<String, Long> e : slugIndex.entrySet()) {
            String slug = e.getKey();
            if (slug.endsWith("/" + t)) {
                return e.getValue();
            }
        }
        return null;
    }

    private List<KbDocument> loadDocs(Long spaceId) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        if (spaceId != null) {
            wrapper.eq(KbDocument::getSpaceId, spaceId);
        } else {
            List<Long> accessible = kbAclService.accessibleSpaceIds();
            if (accessible.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(KbDocument::getSpaceId, accessible);
        }
        wrapper.orderByAsc(KbDocument::getId);
        return kbDocumentMapper.selectList(wrapper);
    }

    private void assertSpaceReadable(Long spaceId) {
        if (spaceId != null) {
            kbAclService.assertCanRead(spaceId);
        }
    }

    private void addLink(Ctx ctx, Long source, Long target, String type) {
        ctx.links.add(new GraphVo.Link(String.valueOf(source), String.valueOf(target), type));
        ctx.degree.merge(source, 1, Integer::sum);
        ctx.degree.merge(target, 1, Integer::sum);
    }

    /** 一次构建、graph 与 lint 复用的中间结果。 */
    private static class Ctx {
        List<KbDocument> docs = new ArrayList<>();
        Map<Long, String> categoryName = new HashMap<>();
        List<GraphVo.Link> links = new ArrayList<>();
        List<LintVo.Broken> broken = new ArrayList<>();
        Map<Long, Set<Long>> inbound = new HashMap<>();
        Map<Long, Integer> degree = new LinkedHashMap<>();
    }
}
