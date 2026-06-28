package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.IngestRawConflictVo;
import com.moli.knowledge.server.dto.RawCoverageItemVo;
import com.moli.knowledge.server.dto.RawCoverageSummaryVo;
import com.moli.knowledge.server.dto.RawCoverageVo;
import com.moli.knowledge.server.exception.IngestRawConflictException;
import com.moli.knowledge.server.entity.KbIngestJob;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbIngestJobMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbRawCoverageService;
import com.moli.knowledge.server.util.KbWikiFrontmatterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class KbRawCoverageServiceImpl implements KbRawCoverageService {

    private static final String DEFAULT_SPACE_CODE = "enterprise-kb";
    private static final String COV_OPEN = "open";
    private static final String COV_COVERED = "covered";
    private static final String COV_CLUSTER = "cluster";
    private static final String MATCH_EXACT = "exact";
    private static final String MATCH_DIR = "dir_prefix";
    private static final String MATCH_NONE = "none";

    private static final Set<String> TERMINAL_JOB_STATUS;
    static {
        Set<String> s = new HashSet<>();
        s.add("committed");
        s.add("cancelled");
        TERMINAL_JOB_STATUS = Collections.unmodifiableSet(s);
    }

    @Resource
    private KbIngestProperties ingestProperties;
    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbIngestJobMapper jobMapper;

    private final ConcurrentHashMap<Long, CachedWikiIndex> wikiIndexCache = new ConcurrentHashMap<>();

    @Override
    public RawCoverageVo coverage(Long spaceId, String prefix, String filter, boolean refresh) {
        assertEnabled();
        KbSpace space = resolveSpace(spaceId);
        kbAclService.assertCanRead(space.getId());

        String cleanFilter = normalizeFilter(filter);
        String cleanPrefix = normalizePrefix(prefix);

        WikiSourceIndex index = loadWikiIndex(space, refresh);
        Map<String, Set<Long>> inFlight = loadInFlightRawPaths(space.getId());

        Path rawRoot = resolveRawRoot();
        Path scanBase = normalizeUnder(rawRoot, cleanPrefix);
        if (!Files.isDirectory(scanBase)) {
            throw new BaseException("目录不存在: " + (cleanPrefix == null ? "" : cleanPrefix));
        }

        RawCoverageSummaryVo summary = new RawCoverageSummaryVo();
        List<RawCoverageItemVo> items = new ArrayList<>();
        int budget = ingestProperties.getMaxCoverageFiles();

        try (Stream<Path> stream = Files.walk(scanBase)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted(Comparator.comparing(p -> rawRoot.relativize(p).toString().replace('\\', '/')))
                    .collect(Collectors.toList());
            for (Path file : files) {
                if (budget <= 0) {
                    log.warn("[raw-coverage] 达到 maxCoverageFiles={}，截断", ingestProperties.getMaxCoverageFiles());
                    break;
                }
                String rel = rawRoot.relativize(file).toString().replace('\\', '/');
                RawCoverageItemVo item = classify(rel, index, inFlight);
                bumpSummary(summary, item.getCoverage());
                if (matchesFilter(cleanFilter, item.getCoverage())) {
                    items.add(item);
                    budget--;
                }
            }
        } catch (IOException e) {
            throw new BaseException("扫描 raw 目录失败：" + e.getMessage());
        }

        RawCoverageVo vo = new RawCoverageVo();
        vo.setSpaceId(space.getId());
        vo.setSpaceCode(space.getSpaceCode());
        vo.setWikiDir(resolveWikiDir(space.getSpaceCode()));
        vo.setIndexedAt(index.indexedAt);
        vo.setWikiPageCount(index.wikiPageCount);
        vo.setFilter(cleanFilter);
        vo.setSummary(summary);
        vo.setItems(items);
        return vo;
    }

    @Override
    public void invalidateCache(Long spaceId) {
        if (spaceId != null) {
            wikiIndexCache.remove(spaceId);
        } else {
            wikiIndexCache.clear();
        }
    }

    @Override
    public void assertRawOpenForCommit(Long spaceId, Long jobId, Set<String> targetWikiSlugs,
                                       Collection<String> rawSourcePaths) {
        if (rawSourcePaths == null || rawSourcePaths.isEmpty()) {
            return;
        }
        KbSpace space = resolveSpace(spaceId);
        WikiSourceIndex index = loadWikiIndex(space, false);
        Map<String, Set<Long>> inFlight = loadInFlightRawPaths(space.getId());
        Set<String> targets = targetWikiSlugs == null ? Collections.emptySet() : targetWikiSlugs;

        List<RawCoverageItemVo> conflicts = new ArrayList<>();
        for (String raw : rawSourcePaths) {
            if (StringUtils.isBlank(raw)) {
                continue;
            }
            String norm = normalizeRawRelPath(raw);
            RawCoverageItemVo item = classify(norm, index, inFlight);
            if (COV_OPEN.equals(item.getCoverage())) {
                continue;
            }
            List<String> wikiSlugs = item.getWikiSlugs();
            if (wikiSlugs == null || wikiSlugs.isEmpty()) {
                continue;
            }
            boolean allInBatch = wikiSlugs.stream().allMatch(targets::contains);
            if (!allInBatch) {
                RawCoverageItemVo conflict = new RawCoverageItemVo();
                conflict.setPath(norm);
                conflict.setCoverage(item.getCoverage());
                conflict.setMatchKind(item.getMatchKind());
                conflict.setWikiSlugs(new ArrayList<>(wikiSlugs));
                conflicts.add(conflict);
            }
        }
        if (!conflicts.isEmpty()) {
            IngestRawConflictVo detail = new IngestRawConflictVo();
            detail.setSpaceId(space.getId());
            detail.setJobId(jobId);
            detail.setConflicts(conflicts);
            throw new IngestRawConflictException(buildRawConflictMessage(conflicts), detail);
        }
    }

    private static String normalizeRawRelPath(String raw) {
        String norm = KbWikiFrontmatterUtil.normalizeRawSourcePath(raw);
        if (norm == null) {
            norm = raw.trim().replace('\\', '/');
            if (norm.startsWith("raw/")) {
                norm = norm.substring(4);
            }
        }
        return norm;
    }

    private static String buildRawConflictMessage(List<RawCoverageItemVo> conflicts) {
        if (conflicts.size() == 1) {
            RawCoverageItemVo c = conflicts.get(0);
            return "raw 已被 wiki 引用，禁止重复 ingest：raw/" + c.getPath()
                    + " → wiki " + c.getWikiSlugs() + "。请对已有页 enrich 或更换 raw 源。";
        }
        StringBuilder sb = new StringBuilder("raw 已被 wiki 引用，禁止重复 ingest（")
                .append(conflicts.size()).append(" 项）：");
        for (int i = 0; i < conflicts.size(); i++) {
            RawCoverageItemVo c = conflicts.get(i);
            if (i > 0) {
                sb.append("；");
            }
            sb.append("raw/").append(c.getPath()).append(" → wiki ").append(c.getWikiSlugs());
        }
        sb.append("。请对已有页 enrich 或更换 raw 源。");
        return sb.toString();
    }

    private RawCoverageItemVo classify(String rawRelPath, WikiSourceIndex index, Map<String, Set<Long>> inFlight) {
        RawCoverageItemVo item = new RawCoverageItemVo();
        item.setPath(rawRelPath);

        Set<String> exactSlugs = index.exactFiles.getOrDefault(rawRelPath, Collections.emptySet());
        if (!exactSlugs.isEmpty()) {
            item.setCoverage(COV_COVERED);
            item.setMatchKind(MATCH_EXACT);
            item.setWikiSlugs(new ArrayList<>(exactSlugs));
        } else {
            Set<String> clusterSlugs = matchDirPrefixes(rawRelPath, index.dirPrefixes);
            if (!clusterSlugs.isEmpty()) {
                item.setCoverage(COV_CLUSTER);
                item.setMatchKind(MATCH_DIR);
                item.setWikiSlugs(new ArrayList<>(clusterSlugs));
            } else {
                item.setCoverage(COV_OPEN);
                item.setMatchKind(MATCH_NONE);
            }
        }

        Set<Long> jobs = new LinkedHashSet<>();
        jobs.addAll(inFlight.getOrDefault(rawRelPath, Collections.emptySet()));
        for (Map.Entry<String, Set<Long>> e : inFlight.entrySet()) {
            if (isUnderPrefix(rawRelPath, e.getKey())) {
                jobs.addAll(e.getValue());
            }
        }
        item.setInFlightJobIds(new ArrayList<>(jobs));
        return item;
    }

    private static Set<String> matchDirPrefixes(String rawRelPath, Map<String, Set<String>> dirPrefixes) {
        Set<String> out = new LinkedHashSet<>();
        String cur = rawRelPath;
        while (true) {
            int slash = cur.lastIndexOf('/');
            if (slash < 0) {
                break;
            }
            cur = cur.substring(0, slash);
            Set<String> slugs = dirPrefixes.get(cur);
            if (slugs != null) {
                out.addAll(slugs);
            }
        }
        Set<String> top = dirPrefixes.get(rawRelPath);
        if (top != null) {
            out.addAll(top);
        }
        return out;
    }

    private static boolean isUnderPrefix(String path, String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return false;
        }
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private static void bumpSummary(RawCoverageSummaryVo summary, String coverage) {
        summary.setTotalFiles(summary.getTotalFiles() + 1);
        if (COV_COVERED.equals(coverage)) {
            summary.setCovered(summary.getCovered() + 1);
        } else if (COV_CLUSTER.equals(coverage)) {
            summary.setCluster(summary.getCluster() + 1);
        } else {
            summary.setOpen(summary.getOpen() + 1);
        }
    }

    private static boolean matchesFilter(String filter, String coverage) {
        if ("all".equals(filter)) {
            return true;
        }
        return filter.equals(coverage);
    }

    private WikiSourceIndex loadWikiIndex(KbSpace space, boolean refresh) {
        if (!refresh) {
            CachedWikiIndex cached = wikiIndexCache.get(space.getId());
            if (cached != null && !cached.expired(ingestProperties.getCoverageCacheSeconds())) {
                return cached.index;
            }
        }
        WikiSourceIndex built = buildWikiIndex(space);
        wikiIndexCache.put(space.getId(), new CachedWikiIndex(built, System.currentTimeMillis()));
        return built;
    }

    private WikiSourceIndex buildWikiIndex(KbSpace space) {
        WikiSourceIndex index = new WikiSourceIndex();
        index.indexedAt = new Date();
        Path wikiBase = resolveWikiBase(space.getSpaceCode());
        if (!Files.isDirectory(wikiBase)) {
            return index;
        }
        try (Stream<Path> stream = Files.walk(wikiBase)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".md"))
                    .forEach(p -> {
                        String stem = p.getFileName().toString();
                        if (stem.endsWith(".md")) {
                            stem = stem.substring(0, stem.length() - 3);
                        }
                        if (KbWikiFrontmatterUtil.isMetaPageStem(stem)) {
                            return;
                        }
                        String slug = wikiBase.relativize(p).toString().replace('\\', '/');
                        if (slug.endsWith(".md")) {
                            slug = slug.substring(0, slug.length() - 3);
                        }
                        index.wikiPageCount++;
                        try {
                            byte[] bytes = Files.readAllBytes(p);
                            String text = new String(bytes, StandardCharsets.UTF_8);
                            for (String src : KbWikiFrontmatterUtil.parseSources(text)) {
                                registerSource(index, slug, src);
                            }
                        } catch (IOException e) {
                            log.warn("[raw-coverage] 读取 wiki 失败 {}: {}", p, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.warn("[raw-coverage] 扫描 wiki 失败: {}", e.getMessage());
        }
        return index;
    }

    private static void registerSource(WikiSourceIndex index, String wikiSlug, String rawSource) {
        String norm = KbWikiFrontmatterUtil.normalizeRawSourcePath(rawSource);
        if (norm == null) {
            return;
        }
        if (KbWikiFrontmatterUtil.isDirectoryLikeRawPath(norm)) {
            index.dirPrefixes.computeIfAbsent(norm, k -> new LinkedHashSet<>()).add(wikiSlug);
        } else {
            index.exactFiles.computeIfAbsent(norm, k -> new LinkedHashSet<>()).add(wikiSlug);
        }
    }

    private Map<String, Set<Long>> loadInFlightRawPaths(Long spaceId) {
        Map<String, Set<Long>> map = new HashMap<>();
        LambdaQueryWrapper<KbIngestJob> w = new LambdaQueryWrapper<>();
        w.eq(KbIngestJob::getSpaceId, spaceId);
        w.eq(KbIngestJob::getIsDelete, CommonConstant.UN_DELETE);
        w.notIn(KbIngestJob::getStatus, TERMINAL_JOB_STATUS);
        List<KbIngestJob> jobs = jobMapper.selectList(w);
        for (KbIngestJob job : jobs) {
            if (StringUtils.isBlank(job.getRawPaths())) {
                continue;
            }
            try {
                List<String> paths = JSON.parseArray(job.getRawPaths(), String.class);
                if (paths == null) {
                    continue;
                }
                for (String p : paths) {
                    if (StringUtils.isBlank(p)) {
                        continue;
                    }
                    map.computeIfAbsent(p.trim().replace('\\', '/'), k -> new LinkedHashSet<>()).add(job.getId());
                }
            } catch (Exception e) {
                log.warn("[raw-coverage] 解析 job rawPaths 失败 id={}: {}", job.getId(), e.getMessage());
            }
        }
        return map;
    }

    private String normalizeFilter(String filter) {
        if (StringUtils.isBlank(filter)) {
            return "all";
        }
        String f = filter.trim().toLowerCase(Locale.ROOT);
        if ("all".equals(f) || COV_OPEN.equals(f) || COV_COVERED.equals(f) || COV_CLUSTER.equals(f)) {
            return f;
        }
        throw new BaseException("filter 非法，允许：all | open | covered | cluster");
    }

    private String normalizePrefix(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return null;
        }
        return prefix.trim().replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private void assertEnabled() {
        if (!ingestProperties.isEnabled()) {
            throw new BaseException("Ingest 工作台未启用");
        }
    }

    private KbSpace resolveSpace(Long spaceId) {
        if (spaceId != null) {
            KbSpace space = kbSpaceMapper.selectById(spaceId);
            if (space == null || (space.getIsDelete() != null && space.getIsDelete() == 1)) {
                throw new BaseException("空间不存在");
            }
            return space;
        }
        LambdaQueryWrapper<KbSpace> w = new LambdaQueryWrapper<>();
        w.eq(KbSpace::getSpaceCode, DEFAULT_SPACE_CODE);
        w.eq(KbSpace::getIsDelete, CommonConstant.UN_DELETE);
        KbSpace space = kbSpaceMapper.selectOne(w);
        if (space == null) {
            throw new BaseException("默认空间不存在: " + DEFAULT_SPACE_CODE);
        }
        return space;
    }

    private Path resolveRawRoot() {
        Path root = Paths.get(ingestProperties.getRawRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.normalize();
    }

    private Path normalizeUnder(Path root, String rel) {
        Path base = root;
        if (StringUtils.isNotBlank(rel)) {
            base = root.resolve(rel).normalize();
        }
        if (!base.startsWith(root)) {
            throw new BaseException("非法路径（越权）: " + rel);
        }
        return base;
    }

    private String resolveWikiDir(String spaceCode) {
        return wikiProperties.getSpaceDirs().getOrDefault(spaceCode, "wiki");
    }

    private Path resolveWikiBase(String spaceCode) {
        Path root = Paths.get(wikiProperties.getRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.resolve(resolveWikiDir(spaceCode)).normalize();
    }

    private static final class WikiSourceIndex {
        private Date indexedAt = new Date();
        private int wikiPageCount;
        private final Map<String, Set<String>> exactFiles = new LinkedHashMap<>();
        private final Map<String, Set<String>> dirPrefixes = new LinkedHashMap<>();
    }

    private static final class CachedWikiIndex {
        private final WikiSourceIndex index;
        private final long builtAtMs;

        private CachedWikiIndex(WikiSourceIndex index, long builtAtMs) {
            this.index = index;
            this.builtAtMs = builtAtMs;
        }

        private boolean expired(int ttlSeconds) {
            return System.currentTimeMillis() - builtAtMs > ttlSeconds * 1000L;
        }
    }
}
