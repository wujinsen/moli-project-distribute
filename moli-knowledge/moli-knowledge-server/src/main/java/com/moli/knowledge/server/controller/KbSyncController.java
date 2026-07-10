package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbDriftReportVo;
import com.moli.knowledge.server.dto.SyncStatusVo;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.service.KbDriftService;
import com.moli.knowledge.server.service.KbSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/sync")
@Api(tags = "kb→DB 同步管理")
public class KbSyncController {

    @Resource
    private KbSyncService kbSyncService;
    @Resource
    private KbDriftService kbDriftService;

    @GetMapping("/drift")
    @ApiOperation("KBOPS-A3 · wiki 磁盘 vs kb_document 漂移检测（按 contentHash）")
    public MoliResult<KbDriftReportVo> drift(@RequestParam Long spaceId,
                                             @RequestParam(required = false) Integer sampleLimit) {
        return MoliResult.success(kbDriftService.drift(spaceId, sampleLimit));
    }

    @GetMapping("/logs")
    @ApiOperation("同步日志分页（需空间管理权限）")
    public MoliResult<Page<KbSyncLog>> logs(@RequestParam(required = false) Long spaceId,
                                            @RequestParam(required = false) String batchNo,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        return MoliResult.success(kbSyncService.logs(spaceId, batchNo, pageNum, pageSize));
    }

    @GetMapping("/status")
    @ApiOperation("最近一批同步统计")
    public MoliResult<SyncStatusVo> status(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbSyncService.status(spaceId));
    }

    @PostMapping("/trigger")
    @ApiOperation("触发 sync_to_db.py 写库")
    public MoliResult<SyncTriggerVo> trigger(@RequestParam(required = false) Long spaceId,
                                             @RequestParam(required = false) String spaceCode) {
        return MoliResult.success(kbSyncService.trigger(spaceId, spaceCode));
    }
}
