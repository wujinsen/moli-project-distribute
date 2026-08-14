package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationDeployPresetsVo;

/**
 * 部署中心预设（SVR-20）。
 */
public interface OperationDeployPresetService {

    OperationDeployPresetsVo getPresets(Long serverId);
}
