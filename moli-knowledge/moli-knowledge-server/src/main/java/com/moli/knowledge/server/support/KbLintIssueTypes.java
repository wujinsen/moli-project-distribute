package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.LintIssueTypeVo;

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

    private static final List<LintIssueTypeVo> DESCRIPTORS = Collections.unmodifiableList(Arrays.asList(
            type(BROKEN_LINK, "断链", "broken_link", false, false),
            type(ORPHAN, "孤儿页", "orphan", false, false),
            type(NO_SUMMARY, "缺摘要（DB summary 为空）", null, true, false),
            type(DUPLICATE, "slug 裸名歧义", "dup_slug", false, false),
            type(STALE, "过时/被取代仍发布", "outdated", false, false),
            type(CONFLICT, "内容 hash 重复", "dup_content", false, false),
            type(MISSING_SOURCE, "缺 sources", "missing_source", false, false),
            type(BAD_TYPE, "type 非法", "bad_type", false, false),
            type(MISSING_TITLE, "缺标题", "missing_title", false, false),
            type(SLUG_MISMATCH, "slug 与路径不一致", "slug_mismatch", false, false),
            type(MISSING_DATES, "缺 created/updated", "missing_dates", false, false),
            type(MISSING_CONCEPT, "缺概念页", "missing_concept", false, false),
            lintPyOnly("near_dup", "近似重复（MinHash）"),
            lintPyOnly("space_branding", "enterprise-kb 含茉莉 branding"),
            lintPyOnly("asym_related", "related 不对称")
    ));

    private KbLintIssueTypes() {
    }

    public static List<String> all() {
        return ALL;
    }

    /** KBOPS-10 · Web issue_type 与 lint.py KIND 对照（含仅 CLI 项说明）。 */
    public static List<LintIssueTypeVo> descriptors() {
        return DESCRIPTORS;
    }

    private static LintIssueTypeVo type(String code, String label, String lintPyKind,
                                       boolean webOnly, boolean lintPyOnly) {
        LintIssueTypeVo vo = new LintIssueTypeVo();
        vo.setCode(code);
        vo.setLabel(label);
        vo.setLintPyKind(lintPyKind);
        vo.setWebOnly(webOnly);
        vo.setLintPyOnly(lintPyOnly);
        return vo;
    }

    private static LintIssueTypeVo lintPyOnly(String lintPyKind, String label) {
        LintIssueTypeVo vo = new LintIssueTypeVo();
        vo.setCode(null);
        vo.setLabel(label);
        vo.setLintPyKind(lintPyKind);
        vo.setWebOnly(false);
        vo.setLintPyOnly(true);
        return vo;
    }
}
