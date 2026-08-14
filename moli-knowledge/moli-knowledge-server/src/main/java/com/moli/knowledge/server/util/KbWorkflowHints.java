package com.moli.knowledge.server.util;

import com.moli.knowledge.server.dto.KbWorkflowHintVo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 跨模块工作流提示（Ingest commit / Sync 后引导 Wiki 治理等）。
 */
public final class KbWorkflowHints {

    public static final String KEY_WIKI_GOVERN_LINT = "wiki_govern_lint";
    public static final String KEY_KB_HEALTH_SCAN = "kb_health_scan";

    private KbWorkflowHints() {
    }

    public static List<KbWorkflowHintVo> afterWikiWrite(Long spaceId) {
        List<KbWorkflowHintVo> hints = new ArrayList<>();
        if (spaceId != null) {
            hints.add(wikiGovernLint(spaceId));
            hints.add(kbHealthScan(spaceId));
        }
        return hints;
    }

    public static KbWorkflowHintVo wikiGovernLint(Long spaceId) {
        KbWorkflowHintVo vo = new KbWorkflowHintVo();
        vo.setKey(KEY_WIKI_GOVERN_LINT);
        vo.setLabel("运行 Wiki 治理 Lint");
        vo.setDescription("扫磁盘 wiki 文件真值，脚本/AI 批量修复 metadata 与断链");
        vo.setRoutePath("knowledge/wiki-govern/index");
        LinkedHashMap<String, String> q = new LinkedHashMap<>();
        q.put("spaceId", String.valueOf(spaceId));
        vo.setRouteQuery(q);
        return vo;
    }

    public static KbWorkflowHintVo kbHealthScan(Long spaceId) {
        KbWorkflowHintVo vo = new KbWorkflowHintVo();
        vo.setKey(KEY_KB_HEALTH_SCAN);
        vo.setLabel("健康体检 · 扫描并落库");
        vo.setDescription("Sync 后更新 DB 快照体检（GET /kb/lint + 扫描并落库）");
        vo.setRoutePath("knowledge/lint/index");
        LinkedHashMap<String, String> q = new LinkedHashMap<>();
        q.put("spaceId", String.valueOf(spaceId));
        vo.setRouteQuery(q);
        return vo;
    }
}
