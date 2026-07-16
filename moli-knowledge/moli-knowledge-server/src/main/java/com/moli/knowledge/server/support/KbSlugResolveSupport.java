package com.moli.knowledge.server.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * 将用户输入的 slug（全路径、末段裸 slug 或标题）解析为 kb_document 行。
 * 与 lint.py resolve()、{@code KbInsightServiceImpl.resolveWikilinkTarget} 对齐。
 */
public final class KbSlugResolveSupport {

    private KbSlugResolveSupport() {
    }

    public static KbDocument findOne(KbDocumentMapper mapper,
                                     Supplier<LambdaQueryWrapper<KbDocument>> scopeFactory,
                                     String slugOrTitle) {
        if (mapper == null || scopeFactory == null || StringUtils.isBlank(slugOrTitle)) {
            return null;
        }
        String trimmed = slugOrTitle.trim();

        KbDocument exact = mapper.selectOne(scopeFactory.get()
                .eq(KbDocument::getSlug, trimmed)
                .last("limit 1"));
        if (exact != null) {
            return exact;
        }

        String bare = bareSlug(trimmed);
        if (StringUtils.isBlank(bare)) {
            return null;
        }

        List<KbDocument> matches = mapper.selectList(scopeFactory.get().and(w -> w
                .eq(KbDocument::getSlug, bare)
                .or().likeLeft(KbDocument::getSlug, "/" + bare)
                .or().eq(KbDocument::getTitle, trimmed)));
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        String bareLower = bare.toLowerCase(Locale.ROOT);
        for (KbDocument d : matches) {
            if (d.getSlug() != null && d.getSlug().equalsIgnoreCase(bare)) {
                return d;
            }
        }
        for (KbDocument d : matches) {
            if (d.getSlug() != null && d.getSlug().toLowerCase(Locale.ROOT).endsWith("/" + bareLower)) {
                return d;
            }
        }
        return matches.get(0);
    }

    static String bareSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            return "";
        }
        String s = slug.trim();
        int slash = s.lastIndexOf('/');
        return slash >= 0 ? s.substring(slash + 1) : s;
    }
}
