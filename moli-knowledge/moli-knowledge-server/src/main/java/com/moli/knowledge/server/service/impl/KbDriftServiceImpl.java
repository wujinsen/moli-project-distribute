package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.KbDriftItemVo;
import com.moli.knowledge.server.dto.KbDriftReportVo;
import com.moli.knowledge.server.dto.KbOpsDriftSummaryVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbDriftService;
import com.moli.knowledge.server.support.KbWikiDriftScanner;
import com.moli.knowledge.server.support.WikiPageSnapshot;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class KbDriftServiceImpl implements KbDriftService {

    private static final int DEFAULT_SAMPLE = 20;
    private static final int MAX_SAMPLE = 100;
    private static final String SOURCE_KB = "kb";

    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbWikiDriftScanner wikiDriftScanner;

    @Override
    public KbDriftReportVo drift(Long spaceId, Integer sampleLimit) {
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        kbAclService.assertCanSyncView(spaceId);
        KbSpace space = kbSpaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BaseException("空间不存在");
        }
        return buildReport(space, normalizeSample(sampleLimit));
    }

    @Override
    public KbOpsDriftSummaryVo driftSummary(Long spaceId, Integer sampleLimit) {
        kbAclService.assertCanOpsDashboard(spaceId);
        int limit = normalizeSample(sampleLimit);
        List<KbSpace> spaces = resolveSpaces(spaceId);
        KbOpsDriftSummaryVo summary = new KbOpsDriftSummaryVo();
        summary.setCheckedAt(new Date());
        summary.setSpacesScanned(spaces.size());

        for (KbSpace space : spaces) {
            try {
                KbDriftReportVo report = buildReport(space, limit);
                summary.getSpaces().add(report);
                summary.setInSyncTotal(summary.getInSyncTotal() + report.getInSyncCount());
                summary.setWikiOnlyTotal(summary.getWikiOnlyTotal() + report.getWikiOnlyCount());
                summary.setDbOnlyTotal(summary.getDbOnlyTotal() + report.getDbOnlyCount());
                summary.setHashMismatchTotal(summary.getHashMismatchTotal() + report.getHashMismatchCount());
                if (report.isDrifted()) {
                    summary.setSpacesWithDrift(summary.getSpacesWithDrift() + 1);
                }
            } catch (Exception e) {
                KbDriftReportVo failed = new KbDriftReportVo();
                failed.setSpaceId(space.getId());
                failed.setSpaceCode(space.getSpaceCode());
                failed.setCheckedAt(new Date());
                failed.setDrifted(true);
                KbDriftItemVo item = new KbDriftItemVo();
                item.setDetail("扫描失败: " + e.getMessage());
                failed.getWikiOnly().add(item);
                summary.getSpaces().add(failed);
                summary.setSpacesWithDrift(summary.getSpacesWithDrift() + 1);
            }
        }
        summary.setDrifted(summary.getSpacesWithDrift() > 0
                || summary.getWikiOnlyTotal() + summary.getDbOnlyTotal() + summary.getHashMismatchTotal() > 0);
        return summary;
    }

    private KbDriftReportVo buildReport(KbSpace space, int sampleLimit) {
        String wikiDir = wikiDriftScanner.resolveWikiDirForSpace(space.getSpaceCode());
        Map<String, WikiPageSnapshot> wiki = wikiDriftScanner.scanWikiDir(wikiDir);
        Map<String, KbDocument> db = loadActiveKbDocs(space.getId());

        List<KbDriftItemVo> wikiOnlyAll = new ArrayList<>();
        List<KbDriftItemVo> dbOnlyAll = new ArrayList<>();
        List<KbDriftItemVo> mismatchAll = new ArrayList<>();
        int inSync = 0;

        TreeSet<String> allSlugs = new TreeSet<>();
        allSlugs.addAll(wiki.keySet());
        allSlugs.addAll(db.keySet());

        for (String slug : allSlugs) {
            WikiPageSnapshot w = wiki.get(slug);
            KbDocument d = db.get(slug);
            if (w != null && d == null) {
                wikiOnlyAll.add(item(slug, null, w.getContentHash(), null,
                        "wiki 有页，DB 无活跃 kb 行（待 Sync）"));
            } else if (w == null && d != null) {
                dbOnlyAll.add(item(slug, d.getTitle(), null, d.getContentHash(),
                        "DB 有 kb 行，wiki 无文件（待删或违规手改 DB）"));
            } else if (w != null) {
                if (StringUtils.equals(w.getContentHash(), d.getContentHash())) {
                    inSync++;
                } else {
                    mismatchAll.add(item(slug, d.getTitle(), w.getContentHash(), d.getContentHash(),
                            "contentHash 不一致（改 wiki 未 Sync 或手改 DB）"));
                }
            }
        }

        KbDriftReportVo vo = new KbDriftReportVo();
        vo.setSpaceId(space.getId());
        vo.setSpaceCode(space.getSpaceCode());
        vo.setWikiDir(wikiDir);
        vo.setCheckedAt(new Date());
        vo.setWikiPageCount(wiki.size());
        vo.setDbKbPageCount(db.size());
        vo.setInSyncCount(inSync);
        vo.setWikiOnlyCount(wikiOnlyAll.size());
        vo.setDbOnlyCount(dbOnlyAll.size());
        vo.setHashMismatchCount(mismatchAll.size());
        vo.setDrifted(vo.getWikiOnlyCount() + vo.getDbOnlyCount() + vo.getHashMismatchCount() > 0);
        vo.setSampleLimit(sampleLimit);
        vo.setWikiOnly(sample(wikiOnlyAll, sampleLimit));
        vo.setDbOnly(sample(dbOnlyAll, sampleLimit));
        vo.setHashMismatches(sample(mismatchAll, sampleLimit));
        return vo;
    }

    private Map<String, KbDocument> loadActiveKbDocs(Long spaceId) {
        List<KbDocument> rows = kbDocumentMapper.selectList(new QueryWrapper<KbDocument>()
                .eq("space_id", spaceId)
                .eq("is_delete", CommonConstant.UN_DELETE)
                .eq("source", SOURCE_KB)
                .select("id", "slug", "title", "content_hash"));
        Map<String, KbDocument> map = new LinkedHashMap<>();
        for (KbDocument row : rows) {
            if (StringUtils.isNotBlank(row.getSlug())) {
                map.put(row.getSlug(), row);
            }
        }
        return map;
    }

    private List<KbSpace> resolveSpaces(Long spaceId) {
        if (spaceId != null) {
            KbSpace space = kbSpaceMapper.selectById(spaceId);
            if (space == null) {
                throw new BaseException("空间不存在");
            }
            return java.util.Collections.singletonList(space);
        }
        List<Long> ids = kbAclService.accessibleSpaceIds();
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return kbSpaceMapper.selectList(new QueryWrapper<KbSpace>()
                .eq("is_delete", CommonConstant.UN_DELETE)
                .in("id", ids));
    }

    private static KbDriftItemVo item(String slug, String title, String wikiHash, String dbHash, String detail) {
        KbDriftItemVo vo = new KbDriftItemVo();
        vo.setSlug(slug);
        vo.setTitle(title);
        vo.setWikiHash(wikiHash);
        vo.setDbHash(dbHash);
        vo.setDetail(detail);
        return vo;
    }

    private static List<KbDriftItemVo> sample(List<KbDriftItemVo> all, int limit) {
        if (all.size() <= limit) {
            return all;
        }
        return new ArrayList<>(all.subList(0, limit));
    }

    private static int normalizeSample(Integer sampleLimit) {
        if (sampleLimit == null || sampleLimit <= 0) {
            return DEFAULT_SAMPLE;
        }
        return Math.min(sampleLimit, MAX_SAMPLE);
    }
}
