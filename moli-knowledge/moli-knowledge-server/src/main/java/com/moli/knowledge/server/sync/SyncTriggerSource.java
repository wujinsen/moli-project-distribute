package com.moli.knowledge.server.sync;

/** Sync 触发来源（告警策略区分用）。 */
public enum SyncTriggerSource {

    /** 定时任务 {@link com.moli.knowledge.server.schedule.KbSyncScheduler} */
    SCHEDULED,

    /** 手动 {@code POST /kb/sync/trigger} */
    MANUAL,

    /** 编辑 wiki 后自动 sync（ingest / 分类移动等） */
    AFTER_EDIT
}
