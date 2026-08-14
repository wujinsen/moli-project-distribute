package com.moli.knowledge.server.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wiki 治理 issue.kind 分类：脚本可修 / 需 LLM / 仅人工。
 */
public final class WikiGovernKindUtil {

    private static final Set<String> SCRIPT_FIXABLE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "missing_dates",
            "slug_mismatch",
            "missing_source"
    )));

    private static final Set<String> AI_FIXABLE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "broken_link",
            "bad_type",
            "missing_title",
            "orphan",
            "missing_concept",
            "outdated",
            "asym_related",
            "near_dup",
            "dup_content"
    )));

    private static final Set<String> MANUAL_ONLY = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "dup_slug"
    )));

    private WikiGovernKindUtil() {
    }

    public static boolean isScriptFixable(String kind) {
        return kind != null && SCRIPT_FIXABLE.contains(kind.trim());
    }

    public static boolean isAiFixable(String kind) {
        return kind != null && AI_FIXABLE.contains(kind.trim());
    }

    public static boolean isManualOnly(String kind) {
        return kind != null && MANUAL_ONLY.contains(kind.trim());
    }

    public static Set<String> scriptFixableKinds() {
        return SCRIPT_FIXABLE;
    }

    public static Set<String> aiFixableKinds() {
        return AI_FIXABLE;
    }

    public static Set<String> manualOnlyKinds() {
        return MANUAL_ONLY;
    }
}
