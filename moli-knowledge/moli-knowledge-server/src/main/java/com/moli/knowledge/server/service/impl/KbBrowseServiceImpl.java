package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.KbTypeCountRow;
import com.moli.knowledge.server.dto.PageDetailVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentTag;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.entity.KbTag;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbDocumentTagMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbTagMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbBrowseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KbBrowseServiceImpl implements KbBrowseService {

    private static final int SEARCH_DEFAULT_LIMIT = 200;

    private static final String[][] TYPE_LABELS = {
            {"guide", "操作指导"}, {"service", "微服务"}, {"concept", "概念"},
            {"article", "技术文章"}, {"interview", "面试题"}, {"output", "综合"},
    };

    private static final Set<String> KNOWN_TYPES = Arrays.stream(TYPE_LABELS)
            .map(t -> t[0])
            .collect(Collectors.toCollection(HashSet::new));

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentTagMapper kbDocumentTagMapper;
    @Resource
    private KbTagMapper kbTagMapper;
    @Resource
    private KbRelationMapper kbRelationMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public IndexTreeVo index(Long spaceId) {
        SpaceScope scope = resolveScope(spaceId);
        if (scope.isEmpty()) {
            return new IndexTreeVo();
        }

        List<KbTypeCountRow> counts = kbDocumentMapper.countPublishedByKbType(
                scope.singleSpaceId,
                scope.multiSpaceIds,
                DocumentStatus.PUBLISHED.getCode());

        Map<String, IndexTreeVo.Group> groups = new LinkedHashMap<>();
        for (String[] tl : TYPE_LABELS) {
            groups.put(tl[0], new IndexTreeVo.Group(tl[0], tl[1]));
        }
        IndexTreeVo.Group other = new IndexTreeVo.Group("other", "其它");

        int total = 0;
        for (KbTypeCountRow row : counts) {
            int cnt = row.getCnt() == null ? 0 : row.getCnt().intValue();
            if (cnt <= 0) {
                continue;
            }
            total += cnt;
            String kbType = row.getKbType();
            if (kbType != null && groups.containsKey(kbType)) {
                groups.get(kbType).setCount(groups.get(kbType).getCount() + cnt);
            } else {
                other.setCount(other.getCount() + cnt);
            }
        }

        IndexTreeVo vo = new IndexTreeVo();
        vo.setTotal(total);
        for (IndexTreeVo.Group g : groups.values()) {
            if (g.getCount() > 0) {
                vo.getGroups().add(g);
            }
        }
        if (other.getCount() > 0) {
            vo.getGroups().add(other);
        }
        return vo;
    }

    @Override
    public IndexItemsPageVo indexItems(Long spaceId, String type, int pageNum, int pageSize) {
        if (StringUtils.isBlank(type)) {
            throw new BaseException("type 不能为空");
        }
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 50;
        }
        if (pageSize > 200) {
            pageSize = 200;
        }

        SpaceScope scope = resolveScope(spaceId);
        if (scope.isEmpty()) {
            return emptyItemsPage(type, pageNum, pageSize);
        }

        LambdaQueryWrapper<KbDocument> wrapper = publishedScopeWrapper(scope);
        applyKbTypeFilter(wrapper, type.trim());
        wrapper.select(KbDocument::getId, KbDocument::getSlug, KbDocument::getTitle, KbDocument::getSpaceId);
        wrapper.orderByAsc(KbDocument::getTitle);

        Page<KbDocument> page = kbDocumentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        IndexItemsPageVo vo = new IndexItemsPageVo();
        vo.setType(type.trim());
        vo.setLabel(typeLabel(type.trim()));
        vo.setTotal(page.getTotal());
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        for (KbDocument d : page.getRecords()) {
            vo.getItems().add(toLightItem(d));
        }
        return vo;
    }

    @Override
    public IndexTreeVo indexSearch(Long spaceId, String q, int limit) {
        String keyword = StringUtils.trimToEmpty(q);
        if (StringUtils.isBlank(keyword)) {
            return index(spaceId);
        }
        if (limit < 1) {
            limit = SEARCH_DEFAULT_LIMIT;
        }
        if (limit > 500) {
            limit = 500;
        }

        SpaceScope scope = resolveScope(spaceId);
        if (scope.isEmpty()) {
            return new IndexTreeVo();
        }

        String pattern = "%" + keyword + "%";
        LambdaQueryWrapper<KbDocument> wrapper = publishedScopeWrapper(scope);
        wrapper.select(KbDocument::getId, KbDocument::getSlug, KbDocument::getTitle, KbDocument::getSpaceId, KbDocument::getKbType);
        wrapper.and(w -> w.like(KbDocument::getTitle, keyword)
                .or().like(KbDocument::getSlug, keyword)
                .or().like(KbDocument::getSummary, pattern));
        wrapper.orderByAsc(KbDocument::getTitle);
        wrapper.last("limit " + limit);

        List<KbDocument> docs = kbDocumentMapper.selectList(wrapper);
        return groupLightItems(docs);
    }

    @Override
    public IndexLocateVo locate(Long spaceId, String slug) {
        if (StringUtils.isBlank(slug)) {
            throw new BaseException("slug 不能为空");
        }
        SpaceScope scope = resolveScope(spaceId);
        if (scope.isEmpty()) {
            throw new BaseException("页面不存在: " + slug);
        }

        LambdaQueryWrapper<KbDocument> wrapper = publishedScopeWrapper(scope);
        wrapper.eq(KbDocument::getSlug, slug.trim());
        wrapper.select(KbDocument::getId, KbDocument::getSlug, KbDocument::getTitle, KbDocument::getSpaceId, KbDocument::getKbType);
        wrapper.last("limit 1");
        KbDocument d = kbDocumentMapper.selectOne(wrapper);
        if (d == null) {
            throw new BaseException("页面不存在: " + slug);
        }

        String type = resolveDocType(d.getKbType());
        IndexLocateVo vo = new IndexLocateVo();
        vo.setType(type);
        vo.setLabel(typeLabel(type));
        vo.setItem(toLightItem(d));
        return vo;
    }

    @Override
    public PageDetailVo page(String slug, Long spaceId) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        wrapper.eq(KbDocument::getSlug, slug);
        if (spaceId != null) {
            wrapper.eq(KbDocument::getSpaceId, spaceId);
        }
        wrapper.last("limit 1");
        KbDocument d = kbDocumentMapper.selectOne(wrapper);
        if (d == null) {
            throw new BaseException("页面不存在: " + slug);
        }
        kbAclService.assertCanRead(d.getSpaceId());
        d.setViewCount(d.getViewCount() == null ? 1 : d.getViewCount() + 1);
        kbDocumentMapper.updateById(d);

        PageDetailVo vo = new PageDetailVo();
        vo.setDocId(d.getId());
        vo.setSpaceId(d.getSpaceId());
        vo.setSlug(d.getSlug());
        vo.setTitle(d.getTitle());
        vo.setSummary(d.getSummary());
        vo.setContent(d.getContent());
        vo.setKbType(d.getKbType());
        vo.setDomain(d.getDomain());
        vo.setStatus(d.getStatus());
        vo.setUpdateTime(d.getUpdateTime());
        vo.setTags(tagNames(d.getId()));
        vo.setOutLinks(links(d.getId(), true));
        vo.setBackLinks(links(d.getId(), false));
        return vo;
    }

    private IndexTreeVo groupLightItems(List<KbDocument> docs) {
        Map<String, IndexTreeVo.Group> groups = new LinkedHashMap<>();
        for (String[] tl : TYPE_LABELS) {
            groups.put(tl[0], new IndexTreeVo.Group(tl[0], tl[1]));
        }
        IndexTreeVo.Group other = new IndexTreeVo.Group("other", "其它");

        for (KbDocument d : docs) {
            String type = resolveDocType(d.getKbType());
            IndexTreeVo.Group g = "other".equals(type) ? other : groups.get(type);
            g.getItems().add(toLightItem(d));
        }

        IndexTreeVo vo = new IndexTreeVo();
        vo.setTotal(docs.size());
        for (IndexTreeVo.Group g : groups.values()) {
            if (!g.getItems().isEmpty()) {
                g.setCount(g.getItems().size());
                vo.getGroups().add(g);
            }
        }
        if (!other.getItems().isEmpty()) {
            other.setCount(other.getItems().size());
            vo.getGroups().add(other);
        }
        return vo;
    }

    private IndexItemsPageVo emptyItemsPage(String type, int pageNum, int pageSize) {
        IndexItemsPageVo vo = new IndexItemsPageVo();
        vo.setType(type);
        vo.setLabel(typeLabel(type));
        vo.setTotal(0);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        return vo;
    }

    private IndexTreeVo.Item toLightItem(KbDocument d) {
        return new IndexTreeVo.Item(d.getId(), d.getSlug(), d.getTitle(), null, d.getSpaceId());
    }

    private String resolveDocType(String kbType) {
        if (kbType != null && KNOWN_TYPES.contains(kbType)) {
            return kbType;
        }
        return "other";
    }

    private String typeLabel(String type) {
        for (String[] tl : TYPE_LABELS) {
            if (tl[0].equals(type)) {
                return tl[1];
            }
        }
        return "其它";
    }

    private void applyKbTypeFilter(LambdaQueryWrapper<KbDocument> wrapper, String type) {
        if ("other".equals(type)) {
            wrapper.and(w -> w.isNull(KbDocument::getKbType)
                    .or().notIn(KbDocument::getKbType, KNOWN_TYPES));
            return;
        }
        wrapper.eq(KbDocument::getKbType, type);
    }

    private LambdaQueryWrapper<KbDocument> publishedScopeWrapper(SpaceScope scope) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        wrapper.eq(KbDocument::getStatus, DocumentStatus.PUBLISHED.getCode());
        if (scope.singleSpaceId != null) {
            wrapper.eq(KbDocument::getSpaceId, scope.singleSpaceId);
        } else if (scope.multiSpaceIds != null) {
            wrapper.in(KbDocument::getSpaceId, scope.multiSpaceIds);
        }
        return wrapper;
    }

    private SpaceScope resolveScope(Long spaceId) {
        if (spaceId != null) {
            kbAclService.assertCanRead(spaceId);
            return SpaceScope.single(spaceId);
        }
        List<Long> accessible = kbAclService.accessibleSpaceIds();
        if (accessible.isEmpty()) {
            return SpaceScope.empty();
        }
        if (accessible.size() == 1) {
            return SpaceScope.single(accessible.get(0));
        }
        return SpaceScope.multi(accessible);
    }

    private List<String> tagNames(Long docId) {
        List<KbDocumentTag> rels = kbDocumentTagMapper.selectList(
                new LambdaQueryWrapper<KbDocumentTag>().eq(KbDocumentTag::getDocumentId, docId));
        if (CollectionUtils.isEmpty(rels)) {
            return new ArrayList<>();
        }
        List<Long> tagIds = rels.stream().map(KbDocumentTag::getTagId).collect(Collectors.toList());
        return kbTagMapper.selectBatchIds(tagIds).stream()
                .map(KbTag::getTagName).collect(Collectors.toList());
    }

    private List<PageDetailVo.Ref> links(Long docId, boolean out) {
        LambdaQueryWrapper<KbRelation> w = new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 1);
        if (out) {
            w.eq(KbRelation::getSourceDocId, docId);
        } else {
            w.eq(KbRelation::getTargetDocId, docId);
        }
        List<KbRelation> rels = kbRelationMapper.selectList(w);
        List<PageDetailVo.Ref> refs = new ArrayList<>();
        for (KbRelation r : rels) {
            Long otherId = out ? r.getTargetDocId() : r.getSourceDocId();
            if (otherId == null) {
                continue;
            }
            KbDocument od = kbDocumentMapper.selectById(otherId);
            if (od != null && CommonConstant.UN_DELETE.equals(od.getIsDelete())) {
                refs.add(new PageDetailVo.Ref(od.getId(), od.getSlug(), od.getTitle(), r.getRelationType()));
            }
        }
        return refs;
    }

    private static final class SpaceScope {
        private final Long singleSpaceId;
        private final List<Long> multiSpaceIds;

        private SpaceScope(Long singleSpaceId, List<Long> multiSpaceIds) {
            this.singleSpaceId = singleSpaceId;
            this.multiSpaceIds = multiSpaceIds;
        }

        static SpaceScope empty() {
            return new SpaceScope(null, null);
        }

        static SpaceScope single(Long spaceId) {
            return new SpaceScope(spaceId, null);
        }

        static SpaceScope multi(List<Long> spaceIds) {
            return new SpaceScope(null, spaceIds);
        }

        boolean isEmpty() {
            return singleSpaceId == null && (multiSpaceIds == null || multiSpaceIds.isEmpty());
        }
    }
}
