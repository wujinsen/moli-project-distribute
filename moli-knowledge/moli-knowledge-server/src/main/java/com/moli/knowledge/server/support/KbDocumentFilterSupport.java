package com.moli.knowledge.server.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbDocument;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 文档搜索 / facet 共用的体裁、分类筛选解析与 QueryWrapper 应用。
 * 列表字段优先于单值字段；分类维 OR（含未分类）。
 */
public final class KbDocumentFilterSupport {

    private KbDocumentFilterSupport() {
    }

    public static final class CategoryFilterScope {
        private final List<Long> categoryIds;
        private final boolean includeUncategorized;

        public CategoryFilterScope(List<Long> categoryIds, boolean includeUncategorized) {
            this.categoryIds = categoryIds == null ? Collections.emptyList() : categoryIds;
            this.includeUncategorized = includeUncategorized;
        }

        public List<Long> getCategoryIds() {
            return categoryIds;
        }

        public boolean isIncludeUncategorized() {
            return includeUncategorized;
        }

        public boolean isActive() {
            return includeUncategorized || CollectionUtils.isNotEmpty(categoryIds);
        }

        public boolean hasCategoryIds() {
            return CollectionUtils.isNotEmpty(categoryIds);
        }
    }

    public static List<String> resolveKbTypes(List<String> kbTypes, String kbType) {
        if (CollectionUtils.isNotEmpty(kbTypes)) {
            Set<String> normalized = new LinkedHashSet<>();
            for (String raw : kbTypes) {
                if (StringUtils.isBlank(raw)) {
                    continue;
                }
                String n = KbTypeConstants.normalize(raw.trim());
                if (n == null) {
                    throw new BaseException("非法体裁 kbType=" + raw
                            + "，可选：" + String.join("|", KbTypeConstants.ALL));
                }
                normalized.add(n);
            }
            return normalized.isEmpty() ? null : new ArrayList<>(normalized);
        }
        if (StringUtils.isNotBlank(kbType)) {
            String n = KbTypeConstants.normalize(kbType.trim());
            if (n == null) {
                throw new BaseException("非法体裁 kbType=" + kbType
                        + "，可选：" + String.join("|", KbTypeConstants.ALL));
            }
            return Collections.singletonList(n);
        }
        return null;
    }

    public static CategoryFilterScope resolveCategoryScope(List<Long> categoryIds,
                                                           Long categoryId,
                                                           Boolean uncategorizedOnly) {
        boolean includeUncategorized = Boolean.TRUE.equals(uncategorizedOnly);
        if (categoryId != null && includeUncategorized) {
            throw new BaseException("categoryId 与 uncategorizedOnly 不能同时传");
        }
        List<Long> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            for (Long id : categoryIds) {
                if (id != null) {
                    ids.add(id);
                }
            }
        } else if (categoryId != null) {
            ids.add(categoryId);
        }
        return new CategoryFilterScope(ids, includeUncategorized);
    }

    public static void applyKbTypes(LambdaQueryWrapper<KbDocument> wrapper, List<String> kbTypes) {
        if (CollectionUtils.isEmpty(kbTypes)) {
            return;
        }
        if (kbTypes.size() == 1) {
            wrapper.eq(KbDocument::getKbType, kbTypes.get(0));
        } else {
            wrapper.in(KbDocument::getKbType, kbTypes);
        }
    }

    public static void applyCategoryScope(LambdaQueryWrapper<KbDocument> wrapper, CategoryFilterScope scope) {
        if (scope == null || !scope.isActive()) {
            return;
        }
        if (scope.hasCategoryIds() && scope.isIncludeUncategorized()) {
            wrapper.and(w -> w.in(KbDocument::getCategoryId, scope.getCategoryIds())
                    .or()
                    .isNull(KbDocument::getCategoryId));
        } else if (scope.isIncludeUncategorized()) {
            wrapper.isNull(KbDocument::getCategoryId);
        } else if (scope.getCategoryIds().size() == 1) {
            wrapper.eq(KbDocument::getCategoryId, scope.getCategoryIds().get(0));
        } else {
            wrapper.in(KbDocument::getCategoryId, scope.getCategoryIds());
        }
    }

    public static void applyCategoryScope(QueryWrapper<KbDocument> wrapper, CategoryFilterScope scope) {
        if (scope == null || !scope.isActive()) {
            return;
        }
        if (scope.hasCategoryIds() && scope.isIncludeUncategorized()) {
            wrapper.and(w -> w.in("category_id", scope.getCategoryIds()).or().isNull("category_id"));
        } else if (scope.isIncludeUncategorized()) {
            wrapper.isNull("category_id");
        } else if (scope.getCategoryIds().size() == 1) {
            wrapper.eq("category_id", scope.getCategoryIds().get(0));
        } else {
            wrapper.in("category_id", scope.getCategoryIds());
        }
    }

    public static void applyKbTypes(QueryWrapper<KbDocument> wrapper, List<String> kbTypes) {
        if (CollectionUtils.isEmpty(kbTypes)) {
            return;
        }
        if (kbTypes.size() == 1) {
            wrapper.eq("kb_type", kbTypes.get(0));
        } else {
            wrapper.in("kb_type", kbTypes);
        }
    }
}
