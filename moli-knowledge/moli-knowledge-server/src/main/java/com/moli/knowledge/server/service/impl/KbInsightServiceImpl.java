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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Override
    public GraphVo graph(Long spaceId) {
        Ctx ctx = build(spaceId);
        GraphVo vo = new GraphVo();
        Set<Long> idSet = new HashSet<>();
        for (KbDocument d : ctx.docs) {
            idSet.add(d.getId());
        }

        // 优先用 kb_relation 落库的边；为空时回退运行时计算（ctx.links）
        List<GraphVo.Link> links = linksFromRelation(spaceId, idSet);
        Map<Long, Integer> degree;
        if (links.isEmpty()) {
            links = ctx.links;
            degree = ctx.degree;
        } else {
            degree = new HashMap<>();
            for (GraphVo.Link l : links) {
                degree.merge(Long.valueOf(l.getSource()), 1, Integer::sum);
                degree.merge(Long.valueOf(l.getTarget()), 1, Integer::sum);
            }
        }

        for (KbDocument d : ctx.docs) {
            GraphVo.Node node = new GraphVo.Node();
            node.setId(String.valueOf(d.getId()));
            node.setTitle(d.getTitle());
            node.setType(nodeType(ctx, d));
            node.setDeg(degree.getOrDefault(d.getId(), 0));
            vo.getNodes().add(node);
        }
        vo.setLinks(links);
        return vo;
    }

    /** 从 kb_relation 读 resolved=1 且两端都在当前文档集合内的边。 */
    private List<GraphVo.Link> linksFromRelation(Long spaceId, Set<Long> idSet) {
        LambdaQueryWrapper<KbRelation> w = new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 1);
        if (spaceId != null) {
            w.eq(KbRelation::getSpaceId, spaceId);
        }
        List<GraphVo.Link> links = new ArrayList<>();
        for (KbRelation r : kbRelationMapper.selectList(w)) {
            if (r.getTargetDocId() == null
                    || !idSet.contains(r.getSourceDocId()) || !idSet.contains(r.getTargetDocId())) {
                continue;
            }
            links.add(new GraphVo.Link(String.valueOf(r.getSourceDocId()),
                    String.valueOf(r.getTargetDocId()), r.getRelationType()));
        }
        return links;
    }

    @Override
    public LintVo lint(Long spaceId) {
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

        // 标题 → 文档ID（wikilink 按标题解析；同名取其一）
        Set<Long> idSet = new HashSet<>();
        Map<String, Long> titleIndex = new HashMap<>();
        for (KbDocument d : ctx.docs) {
            idSet.add(d.getId());
            if (StringUtils.isNotBlank(d.getTitle())) {
                titleIndex.putIfAbsent(d.getTitle().trim().toLowerCase(), d.getId());
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
                Long tid = titleIndex.get(target.toLowerCase());
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

    private List<KbDocument> loadDocs(Long spaceId) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        if (spaceId != null) {
            wrapper.eq(KbDocument::getSpaceId, spaceId);
        }
        wrapper.orderByAsc(KbDocument::getId);
        return kbDocumentMapper.selectList(wrapper);
    }

    private void addLink(Ctx ctx, Long source, Long target, String type) {
        ctx.links.add(new GraphVo.Link(String.valueOf(source), String.valueOf(target), type));
        ctx.degree.merge(source, 1, Integer::sum);
        ctx.degree.merge(target, 1, Integer::sum);
    }

    private String nodeType(Ctx ctx, KbDocument d) {
        if (d.getCategoryId() != null) {
            String cat = ctx.categoryName.get(d.getCategoryId());
            if (StringUtils.isNotBlank(cat)) {
                return cat;
            }
        }
        if (d.getStatus() != null) {
            for (DocumentStatus s : DocumentStatus.values()) {
                if (s.getCode() == d.getStatus()) {
                    return s.getLabel();
                }
            }
        }
        return "未分类";
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
