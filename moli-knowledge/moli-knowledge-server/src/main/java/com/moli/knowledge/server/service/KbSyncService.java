package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.dto.SyncStatusVo;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.entity.KbSyncLog;

public interface KbSyncService {

    /** 分页查同步日志（需空间管理权限或全局管理员）。 */
    Page<KbSyncLog> logs(Long spaceId, String batchNo, int pageNum, int pageSize);

    /** 最近一次同步批次统计。 */
    SyncStatusVo status(Long spaceId);

    /** 触发 kb/tools/sync_to_db.py 写库（需空间管理权限或全局管理员）。 */
    SyncTriggerVo trigger(Long spaceId, String spaceCode);

    /** 编辑类操作（如移动分类）后触发同步：仅需空间编辑权限。 */
    SyncTriggerVo triggerAfterEdit(Long spaceId);

    /** 系统定时触发（无 ACL，仅调度器调用）。 */
    SyncTriggerVo triggerScheduled();
}
