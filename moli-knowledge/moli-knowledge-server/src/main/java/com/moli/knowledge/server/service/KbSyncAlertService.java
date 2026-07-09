package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.sync.SyncTriggerSource;

/**
 * KBOPS-5 · Sync 失败 webhook 告警（飞书 / 企业微信 / 通用 JSON）。
 */
public interface KbSyncAlertService {

    /**
     * 同步失败或异常时尝试发送告警（受 {@code kb.sync.alert.*} 控制）。
     *
     * @param source    触发来源
     * @param spaceCode 空间编码
     * @param result    脚本结果（可为 null，表示未产生结果即异常）
     * @param error     业务异常（可为 null）
     */
    void notifyIfFailed(SyncTriggerSource source, String spaceCode,
                        SyncTriggerVo result, Throwable error);
}
