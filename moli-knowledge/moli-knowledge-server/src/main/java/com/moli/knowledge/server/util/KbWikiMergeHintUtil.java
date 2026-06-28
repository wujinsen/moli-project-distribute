package com.moli.knowledge.server.util;

import com.moli.knowledge.server.dto.WikiGovernMergeHintItemVo;
import com.moli.knowledge.server.dto.WikiLintIssueVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * dup_slug / dup_content 合并提示（P2 · 人工 + Cursor 指令，不调 LLM）。
 */
public final class KbWikiMergeHintUtil {

    private static final Pattern WIKILINK_IN_DETAIL = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final Pattern SLUG_LIST_AFTER_COLON = Pattern.compile("：\\s*(.+)$");

    private KbWikiMergeHintUtil() {
    }

    public static WikiGovernMergeHintItemVo buildHint(WikiLintIssueVo issue) {
        WikiGovernMergeHintItemVo item = new WikiGovernMergeHintItemVo();
        item.setKind(issue.getKind());
        item.setPage(issue.getPage());
        item.setDetail(StringUtils.defaultString(issue.getDetail()));

        List<String> related = new ArrayList<>();
        if ("dup_slug".equals(issue.getKind())) {
            related.addAll(parseSlugListFromDetail(issue.getDetail()));
        } else {
            related.addAll(parseWikilinks(issue.getDetail()));
            if (StringUtils.isNotBlank(issue.getPage()) && !related.contains(issue.getPage())) {
                related.add(0, issue.getPage());
            }
        }
        related = related.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        item.setRelatedSlugs(related);

        String canonical = pickCanonical(related, issue.getPage());
        item.setCanonicalSlug(canonical);

        item.setManualSteps(buildManualSteps(issue.getKind(), canonical, related));
        item.setCursorPrompt(buildCursorPrompt(issue.getKind(), canonical, related, issue.getDetail()));
        return item;
    }

    private static List<String> parseWikilinks(String detail) {
        List<String> out = new ArrayList<>();
        if (StringUtils.isBlank(detail)) {
            return out;
        }
        Matcher m = WIKILINK_IN_DETAIL.matcher(detail);
        while (m.find()) {
            out.add(m.group(1).split("\\|")[0].trim());
        }
        return out;
    }

    private static List<String> parseSlugListFromDetail(String detail) {
        Set<String> out = new LinkedHashSet<>();
        if (StringUtils.isBlank(detail)) {
            return new ArrayList<>();
        }
        Matcher tail = SLUG_LIST_AFTER_COLON.matcher(detail.trim());
        if (tail.find()) {
            String part = tail.group(1);
            for (String seg : part.split("[,，、]")) {
                String s = seg.trim();
                if (s.startsWith("[[")) {
                    s = s.replace("[[", "").replace("]]", "").trim();
                }
                if (StringUtils.isNotBlank(s)) {
                    out.add(s);
                }
            }
        }
        out.addAll(parseWikilinks(detail));
        return new ArrayList<>(out);
    }

    private static String pickCanonical(List<String> slugs, String page) {
        if (StringUtils.isNotBlank(page)) {
            return page;
        }
        if (slugs == null || slugs.isEmpty()) {
            return "";
        }
        return slugs.get(0);
    }

    private static List<String> buildManualSteps(String kind, String canonical, List<String> related) {
        List<String> steps = new ArrayList<>();
        steps.add("确定保留页：" + (StringUtils.isBlank(canonical) ? "（人工指定）" : canonical));
        if (related != null && related.size() > 1) {
            List<String> dup = related.stream()
                    .filter(s -> !StringUtils.equals(s, canonical))
                    .collect(Collectors.toList());
            if (!dup.isEmpty()) {
                steps.add("待合并/删除：" + String.join("、", dup));
            }
        }
        steps.add("在 Cursor/IDE 打开保留页，合并正文与 [[互链]]，更新 frontmatter sources");
        steps.add("删除重复 .md 文件；必要时更新 index.md / graph/edges.jsonl");
        steps.add("保存后运行 Wiki 治理 Lint（脚本修 metadata → 可选 Sync）");
        if ("dup_slug".equals(kind)) {
            steps.add(1, "dup_slug：优先保留路径更规范的一页（如 guides/ 优于 articles/ 重复裸名）");
        }
        return steps;
    }

    private static String buildCursorPrompt(String kind, String canonical, List<String> related, String detail) {
        StringBuilder sb = new StringBuilder();
        sb.append("茉莉知识库 · 合并重复 wiki 页（").append(kind).append("）。\n");
        sb.append("约束：只改 kb/wiki* markdown；禁止改 raw/；保留 frontmatter 规范（AGENTS.md §2）。\n\n");
        if (StringUtils.isNotBlank(detail)) {
            sb.append("Lint 明细：").append(detail).append("\n\n");
        }
        sb.append("建议保留页：").append(StringUtils.defaultIfBlank(canonical, "（请指定）")).append('\n');
        if (related != null && !related.isEmpty()) {
            sb.append("涉及 slug：").append(String.join(", ", related)).append('\n');
        }
        sb.append("\n请执行：\n");
        sb.append("1) 合并正文到保留页，统一 [[互链]] 与 related\n");
        sb.append("2) 删除重复页文件\n");
        sb.append("3) 若需，更新 wiki/index.md 与 wiki/graph/edges.jsonl\n");
        sb.append("4) 输出变更摘要（保留 slug、删除 slug、合并要点）\n");
        return sb.toString();
    }
}
