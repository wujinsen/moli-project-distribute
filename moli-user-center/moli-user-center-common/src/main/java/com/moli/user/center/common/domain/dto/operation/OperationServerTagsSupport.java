package com.moli.user.center.common.domain.dto.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 服务器标签规范化与 JSON 存取。
 */
public final class OperationServerTagsSupport {

    public static final int MAX_TAGS = 20;
    public static final int MAX_TAG_LENGTH = 32;
    private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9:_-]{0,31}$");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<List<String>>() {
    };

    private OperationServerTagsSupport() {
    }

    public static String normalizeTag(String raw) {
        if (raw == null) {
            return null;
        }
        String tag = raw.trim().toLowerCase().replaceAll("\\s+", "-");
        return tag.isEmpty() ? null : tag;
    }

    public static boolean isValidTag(String tag) {
        return tag != null && TAG_PATTERN.matcher(tag).matches();
    }

    public static List<String> normalizeList(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> deduped = new LinkedHashSet<>();
        for (String raw : tags) {
            String tag = normalizeTag(raw);
            if (tag != null) {
                deduped.add(tag);
            }
        }
        return new ArrayList<>(deduped);
    }

    public static boolean isValidList(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return true;
        }
        if (tags.size() > MAX_TAGS) {
            return false;
        }
        for (String raw : tags) {
            String tag = normalizeTag(raw);
            if (tag == null || !isValidTag(tag)) {
                return false;
            }
        }
        return true;
    }

    public static String toJson(List<String> tags) {
        List<String> normalized = normalizeList(tags);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("tags 序列化失败", e);
        }
    }

    public static List<String> parse(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> raw = MAPPER.readValue(json.trim(), STRING_LIST);
            return normalizeList(raw);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static List<String> mergeDistinct(List<List<String>> groups) {
        Set<String> all = new LinkedHashSet<>();
        for (List<String> group : groups) {
            if (group != null) {
                all.addAll(group);
            }
        }
        List<String> sorted = new ArrayList<>(all);
        Collections.sort(sorted);
        return sorted;
    }
}
