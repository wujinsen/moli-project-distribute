package com.moli.knowledge.server.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * kb_lint_issue.issue_type 常量（与 {@code kb/tools/lint.py} KIND_META 对齐）。
 */
public final class KbLintIssueTypes {

    public static final String BROKEN_LINK = "broken_link";
    public static final String ORPHAN = "orphan";
    /** DB summary 列为空（Web 体检特有；lint.py 无等价项）。 */
    public static final String NO_SUMMARY = "no_summary";

    public static final String DUPLICATE = "duplicate";
    public static final String STALE = "stale";
    public static final String CONFLICT = "conflict";

    public static final String MISSING_SOURCE = "missing_source";
    public static final String BAD_TYPE = "bad_type";
    public static final String MISSING_TITLE = "missing_title";
    public static final String SLUG_MISMATCH = "slug_mismatch";
    public static final String MISSING_DATES = "missing_dates";
    public static final String MISSING_CONCEPT = "missing_concept";

    private static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            BROKEN_LINK, ORPHAN, NO_SUMMARY, DUPLICATE, STALE, CONFLICT,
            MISSING_SOURCE, BAD_TYPE, MISSING_TITLE, SLUG_MISMATCH, MISSING_DATES, MISSING_CONCEPT));

    private KbLintIssueTypes() {
    }

    public static List<String> all() {
        return ALL;
    }
}
