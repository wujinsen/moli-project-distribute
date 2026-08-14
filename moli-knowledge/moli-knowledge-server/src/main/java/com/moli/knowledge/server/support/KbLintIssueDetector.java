package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.util.KbWikiFrontmatterUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DB 体检扩展项（KBOPS-8/10）：duplicate / stale / conflict / frontmatter（对齐 lint.py）。
 */
public final class KbLintIssueDetector {

    private KbLintIssueDetector() {
    }

    public static List<LintVo.Duplicate> detectDuplicateSlugs(List<KbDocument> docs) {
        Map<String, List<KbDocument>> groups = new LinkedHashMap<>();
        for (KbDocument doc : docs) {
            String stem = bareSlugStem(doc.getSlug());
            if (stem.isEmpty()) {
                continue;
            }
            groups.computeIfAbsent(stem.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(doc);
        }
        List<LintVo.Duplicate> result = new ArrayList<>();
        for (Map.Entry<String, List<KbDocument>> entry : groups.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            List<String> slugs = entry.getValue().stream()
                    .map(KbDocument::getSlug)
                    .collect(Collectors.toList());
            result.add(new LintVo.Duplicate(
                    entry.getKey(),
                    String.valueOf(entry.getValue().get(0).getId()),
                    entry.getValue().get(0).getTitle(),
                    slugs));
        }
        return result;
    }

    public static List<LintVo.Stale> detectStaleByAge(List<KbDocument> docs, int staleDays) {
        if (staleDays <= 0) {
            return new ArrayList<>();
        }
        Date cutoff = Date.from(Instant.now().minus(staleDays, ChronoUnit.DAYS));
        List<LintVo.Stale> result = new ArrayList<>();
        for (KbDocument doc : docs) {
            if (!isPublished(doc)) {
                continue;
            }
            Date updated = doc.getUpdateTime() != null ? doc.getUpdateTime() : doc.getCreateTime();
            if (updated != null && updated.before(cutoff)) {
                result.add(new LintVo.Stale(
                        String.valueOf(doc.getId()),
                        doc.getTitle(),
                        "超过 " + staleDays + " 天未更新（末次 " + updated + "）"));
            }
        }
        return result;
    }

    public static List<LintVo.Stale> detectSupersededActive(List<KbRelation> relations,
                                                            Map<Long, KbDocument> byId) {
        List<LintVo.Stale> result = new ArrayList<>();
        if (relations == null || relations.isEmpty()) {
            return result;
        }
        for (KbRelation rel : relations) {
            if (!"supersedes".equalsIgnoreCase(rel.getRelationType())) {
                continue;
            }
            KbDocument oldDoc = byId.get(rel.getTargetDocId());
            KbDocument newDoc = byId.get(rel.getSourceDocId());
            if (oldDoc == null || !isPublished(oldDoc)) {
                continue;
            }
            String newLabel = newDoc != null ? newDoc.getSlug() : String.valueOf(rel.getSourceDocId());
            result.add(new LintVo.Stale(
                    String.valueOf(oldDoc.getId()),
                    oldDoc.getTitle(),
                    "被 [[" + newLabel + "]] supersedes 但仍为已发布"));
        }
        return result;
    }

    public static List<LintVo.Conflict> detectContentHashDuplicates(List<KbDocument> docs) {
        Map<String, List<KbDocument>> groups = new HashMap<>();
        for (KbDocument doc : docs) {
            if (StringUtils.isBlank(doc.getContentHash())) {
                continue;
            }
            groups.computeIfAbsent(doc.getContentHash(), k -> new ArrayList<>()).add(doc);
        }
        List<LintVo.Conflict> result = new ArrayList<>();
        for (List<KbDocument> group : groups.values()) {
            if (group.size() < 2) {
                continue;
            }
            List<String> slugs = group.stream().map(KbDocument::getSlug).collect(Collectors.toList());
            result.add(new LintVo.Conflict(
                    String.valueOf(group.get(0).getId()),
                    group.get(0).getTitle(),
                    slugs,
                    "contentHash 相同：" + String.join(", ", slugs)));
        }
        return result;
    }

    /** KBOPS-10 · frontmatter 结构检查（对齐 lint.py ERROR/WARN 子集）。 */
    public static void detectFrontmatterIssues(List<KbDocument> docs, LintVo vo) {
        for (KbDocument doc : docs) {
            if (isMetaPage(doc)) {
                continue;
            }
            String content = doc.getContent() == null ? "" : doc.getContent();
            String pageId = String.valueOf(doc.getId());
            String title = doc.getTitle();

            if (KbWikiFrontmatterUtil.parseSources(content).isEmpty()) {
                vo.getMissingSources().add(item(pageId, title, "sources 为空"));
            }

            String fmType = KbWikiFrontmatterUtil.readField(content, "type");
            String typeToCheck = StringUtils.isNotBlank(fmType) ? fmType : doc.getKbType();
            if (StringUtils.isNotBlank(typeToCheck) && !KbTypeConstants.isValid(typeToCheck)) {
                vo.getBadTypes().add(item(pageId, title, "type=" + typeToCheck));
            }

            if (StringUtils.isBlank(doc.getTitle()) && !KbWikiFrontmatterUtil.hasH1(content)) {
                vo.getMissingTitles().add(item(pageId, title, "无 frontmatter title 且无 H1"));
            }

            String fmSlug = KbWikiFrontmatterUtil.readField(content, "slug");
            String expectedStem = bareSlugStem(doc.getSlug());
            if (StringUtils.isNotBlank(fmSlug) && StringUtils.isNotBlank(expectedStem)
                    && !fmSlug.trim().equals(expectedStem)) {
                vo.getSlugMismatches().add(item(pageId, title,
                        "frontmatter slug=" + fmSlug + " ≠ 路径末段 " + expectedStem));
            }

            String created = KbWikiFrontmatterUtil.readField(content, "created");
            String updated = KbWikiFrontmatterUtil.readField(content, "updated");
            if (StringUtils.isBlank(created) || StringUtils.isBlank(updated)) {
                List<String> miss = new ArrayList<>();
                if (StringUtils.isBlank(created)) {
                    miss.add("created");
                }
                if (StringUtils.isBlank(updated)) {
                    miss.add("updated");
                }
                vo.getMissingDates().add(item(pageId, title, "缺 " + String.join(", ", miss)));
            }
        }
    }

    /** 断链目标被多页引用 → missing_concept（对齐 lint.py missing_concept_min）。 */
    public static List<LintVo.IssueItem> detectMissingConcepts(Map<String, Set<String>> brokenByTarget,
                                                               int missingConceptMin) {
        List<LintVo.IssueItem> result = new ArrayList<>();
        if (brokenByTarget == null || missingConceptMin <= 0) {
            return result;
        }
        for (Map.Entry<String, Set<String>> entry : brokenByTarget.entrySet()) {
            if (entry.getValue().size() >= missingConceptMin) {
                String refs = entry.getValue().stream().sorted().limit(5).collect(Collectors.joining(", "));
                result.add(new LintVo.IssueItem(null, "[[" + entry.getKey() + "]]",
                        "被 " + entry.getValue().size() + " 页引用却无独立页（例：" + refs + "）"));
            }
        }
        return result;
    }

    private static LintVo.IssueItem item(String pageId, String title, String detail) {
        return new LintVo.IssueItem(pageId, title, detail);
    }

    private static boolean isMetaPage(KbDocument doc) {
        return KbWikiFrontmatterUtil.isMetaPageStem(bareSlugStem(doc.getSlug()));
    }

    static String bareSlugStem(String slug) {
        if (StringUtils.isBlank(slug)) {
            return "";
        }
        String s = slug.trim().replace('\\', '/');
        int slash = s.lastIndexOf('/');
        return slash >= 0 ? s.substring(slash + 1) : s;
    }

    private static boolean isPublished(KbDocument doc) {
        return doc.getStatus() != null && doc.getStatus() == 1;
    }
}
