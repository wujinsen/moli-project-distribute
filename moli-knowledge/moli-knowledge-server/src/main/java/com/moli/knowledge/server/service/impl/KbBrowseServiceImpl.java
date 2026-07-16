package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.KbTypeCountRow;
import com.moli.knowledge.server.dto.KbTypeFacetVo;
import com.moli.knowledge.server.dto.PageDetailVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentTag;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.entity.KbTag;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbDocumentTagMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbTagMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbBrowseService;
import com.moli.knowledge.server.support.KbCategoryConstants;
import com.moli.knowledge.server.support.KbDocumentFilterSupport;
import com.moli.knowledge.server.support.KbDocumentFilterSupport.CategoryFilterScope;
import com.moli.knowledge.server.support.KbPublishedWikiFilter;
import com.moli.knowledge.server.support.KbSlugResolveSupport;
import com.moli.knowledge.server.support.KbTypeConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KbBrowseServiceImpl implements KbBrowseService {

    private static final int SEARCH_DEFAULT_LIMIT = 200;

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentTagMapper kbDocumentTagMapper;
    @Resource
    private KbTagMapper kbTagMapper;
    @Resource
    private KbRelationMapper kbRelationMapper;
    @Resource
    private KbCategoryMapper kbCategoryMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public IndexTreeVo index(Long spaceId, List<Long> spaceIds, List<String> kbTypes, String kbType) {
        SpaceScope scope = resolveScope(spaceId, spaceIds);
        if (scope.isEmpty()) {
            return new IndexTreeVo();
        }
        List<String> resolvedKbTypes = KbDocumentFilterSupport.resolveKbTypes(kbTypes, kbType);
        return indexByCategory(scope, resolvedKbTypes);
    }

    /** 按分类（=目录）分组计数。category_id 为空归「未分类」。 */
    private IndexTreeVo indexByCategory(SpaceScope scope, List<String> kbTypes) {
        List<KbCategory> cats = loadScopeCategories(scope);
        Map<String, IndexTreeVo.Group> groups = new LinkedHashMap<>();
        for (KbCategory c : cats) {
            groups.put(c.getId().toString(), new IndexTreeVo.Group(c.getId().toString(), c.getCategoryName()));
        }
        IndexTreeVo.Group uncat = new IndexTreeVo.Group(
                KbCategoryConstants.UNCATEGORIZED_KEY, KbCategoryConstants.UNCATEGORIZED_LABEL);

        int total = 0;
        for (Map.Entry<String, Integer> e : countPublishedByCategory(scope, kbTypes).entrySet()) {
            int cnt = e.getValue() == null ? 0 : e.getValue();
            if (cnt <= 0) {
                continue;
            }
            total += cnt;
            IndexTreeVo.Group g = groups.get(e.getKey());
            if (g == null) {
                uncat.setCount(uncat.getCount() + cnt);
            } else {
                g.setCount(g.getCount() + cnt);
            }
        }

        IndexTreeVo vo = new IndexTreeVo();
        vo.setTotal(total);
        for (IndexTreeVo.Group g : groups.values()) {
            if (g.getCount() > 0) {
                vo.getGroups().add(g);
            }
        }
        if (uncat.getCount() > 0) {
            vo.getGroups().add(uncat);
        }
        return vo;
    }

    @Override
    public KbTypeFacetVo types(Long spaceId, List<Long> spaceIds, List<Long> categoryIds, Long categoryId,
                               Boolean uncategorizedOnly) {
        CategoryFilterScope categoryScope = KbDocumentFilterSupport.resolveCategoryScope(
                categoryIds, categoryId, uncategorizedOnly);
        KbTypeFacetVo vo = new KbTypeFacetVo();
        SpaceScope scope = resolveScope(spaceId, spaceIds);
        if (scope.isEmpty()) {
            return vo;
        }
        List<KbTypeCountRow> rows = kbDocumentMapper.countPublishedByKbType(
                scope.singleSpaceId,
                scope.multiSpaceIds,
                DocumentStatus.PUBLISHED.getCode(),
                categoryScope.getCategoryIds(),
                categoryScope.isIncludeUncategorized());
        Map<String, Long> byType = new LinkedHashMap<>();
        for (KbTypeCountRow r : rows) {
            if (r == null || r.getCnt() == null) {
                continue;
            }
            String t = r.getKbType() == null ? "" : r.getKbType();
            byType.merge(t, r.getCnt(), Long::sum);
        }
        long total = 0;
        // 先按白名单顺序输出，未知/空体裁追加在后
        for (String t : KbTypeConstants.ALL) {
            Long c = byType.remove(t);
            if (c != null && c > 0) {
                vo.getItems().add(new KbTypeFacetVo.Item(t, KbTypeConstants.label(t), c));
                total += c;
            }
        }
        for (Map.Entry<String, Long> e : byType.entrySet()) {
            if (e.getValue() != null && e.getValue() > 0 && StringUtils.isNotBlank(e.getKey())) {
                vo.getItems().add(new KbTypeFacetVo.Item(e.getKey(), KbTypeConstants.label(e.getKey()), e.getValue()));
                total += e.getValue();
            }
        }
        vo.setTotal(total);
        return vo;
    }

    @Override
    public IndexItemsPageVo indexItems(Long spaceId, List<Long> spaceIds, String key, int pageNum, int pageSize) {
        if (StringUtils.isBlank(key)) {
            throw new BaseException("分组 key 不能为空");
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
        String k = key.trim();

        SpaceScope scope = resolveScope(spaceId, spaceIds);
        if (scope.isEmpty()) {
            return emptyItemsPage(k, pageNum, pageSize);
        }

        LambdaQueryWrapper<KbDocument> wrapper = publishedScopeWrapper(scope);
        applyCategoryFilter(wrapper, k);
        String label = categoryLabel(scope, k);
        wrapper.select(KbDocument::getId, KbDocument::getSlug, KbDocument::getTitle, KbDocument::getSpaceId);
        wrapper.orderByAsc(KbDocument::getTitle);

        Page<KbDocument> page = kbDocumentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        IndexItemsPageVo vo = new IndexItemsPageVo();
        vo.setType(k);
        vo.setLabel(label);
        vo.setTotal(page.getTotal());
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        for (KbDocument d : page.getRecords()) {
            vo.getItems().add(toLightItem(d));
        }
        return vo;
    }

    @Override
    public IndexTreeVo indexSearch(Long spaceId, List<Long> spaceIds, String q, int limit) {
        String keyword = StringUtils.trimToEmpty(q);
        if (StringUtils.isBlank(keyword)) {
            return index(spaceId, spaceIds, null, null);
        }
        if (limit < 1) {
            limit = SEARCH_DEFAULT_LIMIT;
        }
        if (limit > 500) {
            limit = 500;
        }

        SpaceScope scope = resolveScope(spaceId, spaceIds);
        if (scope.isEmpty()) {
            return new IndexTreeVo();
        }

        String pattern = "%" + keyword + "%";
        LambdaQueryWrapper<KbDocument> wrapper = publishedScopeWrapper(scope);
        wrapper.select(KbDocument::getId, KbDocument::getSlug, KbDocument::getTitle, KbDocument::getSpaceId,
                KbDocument::getCategoryId);
        wrapper.and(w -> w.like(KbDocument::getTitle, keyword)
                .or().like(KbDocument::getSlug, keyword)
                .or().like(KbDocument::getSummary, pattern));
        wrapper.orderByAsc(KbDocument::getTitle);
        wrapper.last("limit " + limit);

        List<KbDocument> docs = kbDocumentMapper.selectList(wrapper);
        return groupLightItemsByCategory(scope, docs);
    }

    @Override
    public IndexLocateVo locate(Long spaceId, List<Long> spaceIds, String slug) {
        if (StringUtils.isBlank(slug)) {
            throw new BaseException("slug 不能为空");
        }
        SpaceScope scope = resolveScope(spaceId, spaceIds);
        if (scope.isEmpty()) {
            throw new BaseException("页面不存在: " + slug);
        }

        KbDocument d = KbSlugResolveSupport.findOne(kbDocumentMapper, () -> {
            LambdaQueryWrapper<KbDocument> w = publishedScopeWrapper(scope);
            w.select(KbDocument::getId, KbDocument::getSlug, KbDocument::getTitle, KbDocument::getSpaceId,
                    KbDocument::getCategoryId);
            return w;
        }, slug);
        if (d == null) {
            throw new BaseException("页面不存在: " + slug);
        }

        String key = d.getCategoryId() == null
                ? KbCategoryConstants.UNCATEGORIZED_KEY
                : d.getCategoryId().toString();
        IndexLocateVo vo = new IndexLocateVo();
        vo.setType(key);
        vo.setLabel(categoryLabel(scope, key));
        vo.setItem(toLightItem(d));
        return vo;
    }

    @Override
    public PageDetailVo page(String slug, Long spaceId) {
        KbDocument d = KbSlugResolveSupport.findOne(kbDocumentMapper, () -> {
            LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
            if (spaceId != null) {
                wrapper.eq(KbDocument::getSpaceId, spaceId);
            }
            return wrapper;
        }, slug);
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

    private IndexItemsPageVo emptyItemsPage(String key, int pageNum, int pageSize) {
        IndexItemsPageVo vo = new IndexItemsPageVo();
        vo.setType(key);
        vo.setLabel(KbCategoryConstants.UNCATEGORIZED_LABEL);
        vo.setTotal(0);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        return vo;
    }

    private IndexTreeVo.Item toLightItem(KbDocument d) {
        return new IndexTreeVo.Item(d.getId(), d.getSlug(), d.getTitle(), null, d.getSpaceId());
    }

    private List<KbCategory> loadScopeCategories(SpaceScope scope) {
        LambdaQueryWrapper<KbCategory> w = new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE)
                .orderByAsc(KbCategory::getSort)
                .orderByAsc(KbCategory::getId);
        if (scope.singleSpaceId != null) {
            w.eq(KbCategory::getSpaceId, scope.singleSpaceId);
        } else if (scope.multiSpaceIds != null) {
            w.in(KbCategory::getSpaceId, scope.multiSpaceIds);
        }
        return kbCategoryMapper.selectList(w);
    }

    /** 统计已发布(source=kb)文档按 category_id 分组数；null 归 uncategorized。 */
    private Map<String, Integer> countPublishedByCategory(SpaceScope scope, List<String> kbTypes) {
        QueryWrapper<KbDocument> qw = KbPublishedWikiFilter.publishedKbQuery(scope.singleSpaceId);
        if (scope.multiSpaceIds != null) {
            qw.in("space_id", scope.multiSpaceIds);
        }
        KbDocumentFilterSupport.applyKbTypes(qw, kbTypes);
        qw.select("category_id AS category_id", "count(*) AS cnt");
        qw.groupBy("category_id");
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> row : kbDocumentMapper.selectMaps(qw)) {
            Object cid = row.get("category_id");
            Object cnt = row.get("cnt");
            String key = cid == null
                    ? KbCategoryConstants.UNCATEGORIZED_KEY
                    : cid.toString();
            map.merge(key, cnt == null ? 0 : Integer.valueOf(cnt.toString()), Integer::sum);
        }
        return map;
    }

    private void applyCategoryFilter(LambdaQueryWrapper<KbDocument> wrapper, String key) {
        if (KbCategoryConstants.UNCATEGORIZED_KEY.equals(key)) {
            wrapper.isNull(KbDocument::getCategoryId);
            return;
        }
        try {
            wrapper.eq(KbDocument::getCategoryId, Long.valueOf(key));
        } catch (NumberFormatException e) {
            throw new BaseException("非法分类 key: " + key);
        }
    }

    private String categoryLabel(SpaceScope scope, String key) {
        if (KbCategoryConstants.UNCATEGORIZED_KEY.equals(key)) {
            return KbCategoryConstants.UNCATEGORIZED_LABEL;
        }
        for (KbCategory c : loadScopeCategories(scope)) {
            if (c.getId().toString().equals(key)) {
                return c.getCategoryName();
            }
        }
        return KbCategoryConstants.UNCATEGORIZED_LABEL;
    }

    private IndexTreeVo groupLightItemsByCategory(SpaceScope scope, List<KbDocument> docs) {
        Map<String, IndexTreeVo.Group> groups = new LinkedHashMap<>();
        for (KbCategory c : loadScopeCategories(scope)) {
            groups.put(c.getId().toString(), new IndexTreeVo.Group(c.getId().toString(), c.getCategoryName()));
        }
        IndexTreeVo.Group uncat = new IndexTreeVo.Group(
                KbCategoryConstants.UNCATEGORIZED_KEY, KbCategoryConstants.UNCATEGORIZED_LABEL);
        for (KbDocument d : docs) {
            String key = d.getCategoryId() == null
                    ? KbCategoryConstants.UNCATEGORIZED_KEY
                    : d.getCategoryId().toString();
            IndexTreeVo.Group g = groups.getOrDefault(key, uncat);
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
        if (!uncat.getItems().isEmpty()) {
            uncat.setCount(uncat.getItems().size());
            vo.getGroups().add(uncat);
        }
        return vo;
    }

    private LambdaQueryWrapper<KbDocument> publishedScopeWrapper(SpaceScope scope) {
        LambdaQueryWrapper<KbDocument> wrapper = KbPublishedWikiFilter.publishedKbWrapper(scope.singleSpaceId);
        if (scope.multiSpaceIds != null) {
            wrapper.in(KbDocument::getSpaceId, scope.multiSpaceIds);
        }
        return wrapper;
    }

    private SpaceScope resolveScope(Long spaceId, List<Long> spaceIds) {
        List<Long> scopeSpaces = kbAclService.resolveReadableSpaceIds(spaceId, spaceIds);
        if (scopeSpaces.isEmpty()) {
            return SpaceScope.empty();
        }
        if (scopeSpaces.size() == 1) {
            return SpaceScope.single(scopeSpaces.get(0));
        }
        return SpaceScope.multi(scopeSpaces);
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
