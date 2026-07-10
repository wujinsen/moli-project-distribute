package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationDeployPresetItemVo;
import com.moli.user.center.common.domain.vo.OperationDeployPresetsVo;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import com.moli.user.center.server.operation.guard.OperationPathPolicy;
import com.moli.user.center.server.operation.service.OperationDeployPresetService;
import com.moli.user.center.server.operation.service.OperationServerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
public class OperationDeployPresetServiceImpl implements OperationDeployPresetService {

    private static final List<OperationDeployPresetItemVo> ACTION_PRESETS = Arrays.asList(
            new OperationDeployPresetItemVo("none", "无"),
            new OperationDeployPresetItemVo("nginxReload", "sudo nginx -s reload"),
            new OperationDeployPresetItemVo("unzipToDist", "解压 zip 到 dist（备份旧目录）"),
            new OperationDeployPresetItemVo("restartService:user-center", "重启 user-center"),
            new OperationDeployPresetItemVo("restartService:gateway", "重启 gateway"),
            new OperationDeployPresetItemVo("restartService:knowledge", "重启 knowledge")
    );

    @Resource
    private OperationUploadProperties uploadProperties;
    @Resource
    private OperationServerService operationServerService;

    @Override
    public OperationDeployPresetsVo getPresets(Long serverId) {
        OperationServerInfo server = serverId != null ? operationServerService.requireEntity(serverId) : null;
        OperationDeployPresetsVo vo = new OperationDeployPresetsVo();
        vo.setPathPresets(OperationPathPolicy.pathPresets(server, uploadProperties));
        vo.setActionPresets(ACTION_PRESETS);
        return vo;
    }
}
