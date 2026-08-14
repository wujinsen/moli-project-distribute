package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbWikiGraphService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 从磁盘 wiki 目录构建图谱：正文 [[wikilink]]、frontmatter related、graph/edges.jsonl。
 * 节点 id = slug（与 serve.py 一致，非文档数字 ID）。
 */
@Slf4j
@Service
public class KbWikiGraphServiceImpl implements KbWikiGraphService {

    private static final String MODE_SUMMARY = "summary";
    private static final String MODE_FULL = "full";
    private static final int GRAPH_DEFAULT_MAX_NODES = 300;
    private static final int GRAPH_SUMMARY_TOP = 50;
    private static final int GRAPH_MAX_NODES_CAP = 2000;

    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final Pattern FM_TITLE = Pattern.compile("^title:\\s*(.+)$", Pattern.MULTILINE);
    private static final Pattern FM_TYPE = Pattern.compile("^type:\\s*(\\S+).*$", Pattern.MULTILINE);
    private static final Pattern FM_RELATED = Pattern.compile("^related:\\s*\\[(.*)]\\s*$", Pattern.MULTILINE);
    private static final Set<String> SPECIAL_STEMS = new HashSet<String>(Arrays.asList("index", "log"));

    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public GraphVo graph(Long spaceId, String mode, Integer maxNodes, Integer minDeg) {
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空（Wiki 图谱需指定单个空间）");
        }
        kbAclService.assertCanRead(spaceId);
        KbSpace space = kbSpaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BaseException("空间不存在");
        }
        Path base = resolveWikiRoot(space.getSpaceCode());
        boolean summary = MODE_SUMMARY.equalsIgnoreCase(mode);
        if (!Files.isDirectory(base)) {
            return emptyGraph(summary ? MODE_SUMMARY : MODE_FULL);
        }

        Map<String, WikiPage> pages = loadPages(base);
        List<GraphVo.Link> allLinks = buildLinks(base, pages);
        Map<String, Integer> degree = degreeOf(allLinks, pages.values().stream()
                .filter(p -> !p.special)
                .map(p -> p.slug)
                .collect(Collectors.toSet()));

        int effMax = summary
                ? (maxNodes == null || maxNodes <= 0 ? GRAPH_SUMMARY_TOP : Math.min(maxNodes, GRAPH_MAX_NODES_CAP))
                : (maxNodes == null || maxNodes <= 0 ? GRAPH_DEFAULT_MAX_NODES : Math.min(maxNodes, GRAPH_MAX_NODES_CAP));
        int effMinDeg = minDeg == null ? 0 : Math.max(0, minDeg);

        List<String> ranked = degree.entrySet().stream()
                .filter(e -> e.getValue() >= effMinDeg)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        boolean truncated = ranked.size() > effMax;
        List<String> keepSlugs = truncated ? ranked.subList(0, effMax) : ranked;
        Set<String> keepSet = new HashSet<>(keepSlugs);

        GraphVo vo = new GraphVo();
        GraphVo.Meta meta = vo.getMeta();
        meta.setTotalNodes((int) pages.values().stream().filter(p -> !p.special).count());
        meta.setTotalLinks(allLinks.size());
        meta.setSource("wiki_file");
        meta.setMode(summary ? MODE_SUMMARY : MODE_FULL);

        for (String slug : keepSlugs) {
            WikiPage p = pages.get(slug);
            if (p == null || p.special) {
                continue;
            }
            vo.getNodes().add(new GraphVo.Node(slug, p.title, p.type, degree.getOrDefault(slug, 0)));
        }
        for (GraphVo.Link l : allLinks) {
            if (keepSet.contains(l.getSource()) && keepSet.contains(l.getTarget())) {
                vo.getLinks().add(l);
            }
        }
        meta.setReturnedNodes(vo.getNodes().size());
        meta.setReturnedLinks(vo.getLinks().size());
        meta.setTruncated(truncated);
        return vo;
    }

    private Path resolveWikiRoot(String spaceCode) {
        String wikiDir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(wikiDir)) {
            throw new BaseException("空间未配置 wiki 目录映射: " + spaceCode);
        }
        Path root = com.moli.knowledge.server.util.KbRepoPathUtil.resolveKbRoot(wikiProperties.getRoot());
        return root.resolve(wikiDir).normalize();
    }

    private GraphVo emptyGraph(String mode) {
        GraphVo vo = new GraphVo();
        GraphVo.Meta meta = vo.getMeta();
        meta.setSource("wiki_file");
        meta.setMode(mode);
        return vo;
    }

    private Map<String, WikiPage> loadPages(Path wikiRoot) {
        Map<String, WikiPage> pages = new LinkedHashMap<>();
        try {
            Files.walkFileTree(wikiRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!file.getFileName().toString().endsWith(".md")) {
                        return FileVisitResult.CONTINUE;
                    }
                    try {
                        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                        String[] parts = splitFrontmatter(text);
                        String fm = parts[0];
                        String body = parts[1];
                        String stem = file.getFileName().toString().replaceFirst("\\.md$", "");
                        boolean special = SPECIAL_STEMS.contains(stem);
                        String slug = extractMeta(fm, "slug");
                        if (StringUtils.isBlank(slug)) {
                            slug = stem;
                        }
                        if (!special) {
                            Path rel = wikiRoot.relativize(file);
                            if (rel.getNameCount() > 1) {
                                String pathSlug = rel.toString().replace('\\', '/').replaceFirst("\\.md$", "");
                                if (StringUtils.isNotBlank(pathSlug)) {
                                    slug = pathSlug;
                                }
                            }
                        }
                        String key = special ? stem : slug;
                        String title = extractMeta(fm, "title");
                        if (StringUtils.isBlank(title)) {
                            title = firstH1(body);
                        }
                        if (StringUtils.isBlank(title)) {
                            title = stem;
                        }
                        String type = extractMeta(fm, "type");
                        if (StringUtils.isBlank(type)) {
                            type = special ? "meta" : "concept";
                        }
                        WikiPage p = new WikiPage();
                        p.slug = key;
                        p.title = title.trim();
                        p.type = type.trim();
                        p.body = body;
                        p.related = parseRelatedList(fm);
                        p.special = special;
                        pages.put(key, p);
                    } catch (Exception e) {
                        log.warn("跳过无法解析的 wiki 页: {}", file, e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new BaseException("扫描 wiki 目录失败：" + e.getMessage());
        }
        return pages;
    }

    private List<GraphVo.Link> buildLinks(Path wikiRoot, Map<String, WikiPage> pages) {
        Set<String> slugs = pages.values().stream()
                .filter(p -> !p.special)
                .map(p -> p.slug)
                .collect(Collectors.toSet());
        Set<String> slugTails = slugs.stream().map(this::slugTail).collect(Collectors.toSet());
        Map<String, String> tailToSlug = new HashMap<>();
        for (String s : slugs) {
            tailToSlug.putIfAbsent(slugTail(s).toLowerCase(Locale.ROOT), s);
        }

        Map<String, String> typed = new LinkedHashMap<>();

        for (WikiPage p : pages.values()) {
            if (p.special) {
                continue;
            }
            Matcher m = WIKILINK.matcher(p.body == null ? "" : p.body);
            while (m.find()) {
                String target = m.group(1).split("\\|")[0].trim();
                String resolved = resolveSlug(target, slugs, slugTails, tailToSlug);
                if (resolved != null && !resolved.equals(p.slug)) {
                    typed.putIfAbsent(linkKey(p.slug, resolved), "links_to");
                }
            }
            for (String rel : p.related) {
                String resolved = resolveSlug(rel, slugs, slugTails, tailToSlug);
                if (resolved != null && !resolved.equals(p.slug)) {
                    typed.putIfAbsent(linkKey(p.slug, resolved), "relates_to");
                }
            }
        }

        for (JSONObject e : loadEdgesFromPath(wikiRoot)) {
            String from = slugTail(e.getString("from"));
            String to = slugTail(e.getString("to"));
            String fromSlug = tailToSlug.get(from.toLowerCase(Locale.ROOT));
            String toSlug = tailToSlug.get(to.toLowerCase(Locale.ROOT));
            if (fromSlug != null && toSlug != null) {
                typed.put(linkKey(fromSlug, toSlug), e.getString("type") == null ? "relates_to" : e.getString("type"));
            }
        }

        List<GraphVo.Link> links = new ArrayList<>();
        for (Map.Entry<String, String> e : typed.entrySet()) {
            String[] parts = e.getKey().split("->", 2);
            if (parts.length == 2) {
                links.add(new GraphVo.Link(parts[0], parts[1], e.getValue()));
            }
        }
        return links;
    }

    private List<JSONObject> loadEdgesFromPath(Path wikiRoot) {
        Path edgesFile = wikiRoot.resolve("graph").resolve("edges.jsonl");
        if (!Files.exists(edgesFile)) {
            return Collections.emptyList();
        }
        List<JSONObject> out = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(edgesFile, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    out.add(JSON.parseObject(trimmed));
                } catch (Exception ignore) {
                    // 跳过坏行
                }
            }
        } catch (IOException e) {
            log.warn("读取 edges.jsonl 失败: {}", edgesFile, e);
        }
        return out;
    }

    private String linkKey(String from, String to) {
        return from + "->" + to;
    }

    private String slugTail(String slug) {
        if (StringUtils.isBlank(slug)) {
            return "";
        }
        int i = slug.lastIndexOf('/');
        return i >= 0 ? slug.substring(i + 1) : slug;
    }

    private String resolveSlug(String target, Set<String> slugs, Set<String> slugTails, Map<String, String> tailToSlug) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        String t = target.trim();
        if (slugs.contains(t)) {
            return t;
        }
        String lower = t.toLowerCase(Locale.ROOT);
        if (slugTails.contains(t) || tailToSlug.containsKey(lower)) {
            return tailToSlug.get(lower);
        }
        int slash = t.lastIndexOf('/');
        if (slash >= 0) {
            String tail = t.substring(slash + 1).toLowerCase(Locale.ROOT);
            if (tailToSlug.containsKey(tail)) {
                return tailToSlug.get(tail);
            }
        }
        for (String s : slugs) {
            if (s.endsWith("/" + t) || s.equalsIgnoreCase(t)) {
                return s;
            }
        }
        return null;
    }

    private Map<String, Integer> degreeOf(List<GraphVo.Link> links, Set<String> allSlugs) {
        Map<String, Integer> deg = new HashMap<>();
        for (String s : allSlugs) {
            deg.put(s, 0);
        }
        for (GraphVo.Link l : links) {
            deg.merge(l.getSource(), 1, Integer::sum);
            deg.merge(l.getTarget(), 1, Integer::sum);
        }
        return deg;
    }

    private static String[] splitFrontmatter(String text) {
        if (text == null) {
            return new String[]{"", ""};
        }
        String trimmed = text.trim();
        if (!trimmed.startsWith("---")) {
            return new String[]{"", text};
        }
        int end = trimmed.indexOf("\n---", 3);
        if (end < 0) {
            return new String[]{"", text};
        }
        String fm = trimmed.substring(3, end).trim();
        String body = trimmed.substring(end + 4).trim();
        return new String[]{fm, body};
    }

    private static String extractMeta(String fm, String key) {
        if (StringUtils.isBlank(fm)) {
            return null;
        }
        if ("title".equals(key)) {
            Matcher m = FM_TITLE.matcher(fm);
            return m.find() ? m.group(1).trim() : null;
        }
        if ("type".equals(key)) {
            Matcher m = FM_TYPE.matcher(fm);
            return m.find() ? m.group(1).trim() : null;
        }
        if ("slug".equals(key)) {
            Pattern p = Pattern.compile("^slug:\\s*(.+)$", Pattern.MULTILINE);
            Matcher m = p.matcher(fm);
            return m.find() ? m.group(1).trim() : null;
        }
        return null;
    }

    private static List<String> parseRelatedList(String fm) {
        if (StringUtils.isBlank(fm)) {
            return Collections.emptyList();
        }
        Matcher m = FM_RELATED.matcher(fm);
        if (!m.find()) {
            return Collections.emptyList();
        }
        String inner = m.group(1).trim();
        if (inner.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (String part : inner.split(",")) {
            String s = part.trim().replaceAll("^[\"']|[\"']$", "");
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }

    private static String firstH1(String body) {
        if (body == null) {
            return null;
        }
        for (String line : body.split("\n")) {
            if (line.startsWith("# ")) {
                return line.substring(2).trim();
            }
        }
        return null;
    }

    private static class WikiPage {
        String slug;
        String title;
        String type;
        String body;
        List<String> related = Collections.emptyList();
        boolean special;
    }
}
