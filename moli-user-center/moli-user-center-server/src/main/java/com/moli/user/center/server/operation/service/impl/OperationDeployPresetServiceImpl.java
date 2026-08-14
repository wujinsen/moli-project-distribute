package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationDeployPresetItemVo;
import com.moli.user.center.common.domain.vo.OperationDeployPresetsVo;
import com.moli.user.center.common.domain.vo.OperationDeployServiceOptionVo;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.guard.OperationPathPolicy;
import com.moli.user.center.server.operation.service.OperationDeployPresetService;
import com.moli.user.center.server.operation.service.OperationServerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationDeployPresetServiceImpl implements OperationDeployPresetService {

    private static final List<OperationDeployPresetItemVo> BASE_ACTION_PRESETS = Arrays.asList(
            new OperationDeployPresetItemVo("none", "无"),
            new OperationDeployPresetItemVo("nginxReload", "sudo nginx -s reload"),
            new OperationDeployPresetItemVo("unzipToDist", "解压 zip 到 dist（备份旧目录）")
    );

    @Resource
    private OperationUploadProperties uploadProperties;
    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationDeployServiceRegistry deployServiceRegistry;

    @Override
    public OperationDeployPresetsVo getPresets(Long serverId) {
        OperationServerInfo server = serverId != null ? operationServerService.requireEntity(serverId) : null;
        OperationDeployPresetsVo vo = new OperationDeployPresetsVo();
        vo.setPathPresets(OperationPathPolicy.pathPresets(server, uploadProperties));
        vo.setActionPresets(buildActionPresets());
        vo.setServiceKeys(buildServiceOptions());
        return vo;
    }

    private List<OperationDeployPresetItemVo> buildActionPresets() {
        List<OperationDeployPresetItemVo> presets = new ArrayList<>(BASE_ACTION_PRESETS);
        for (OperationDeployServiceOptionVo option : buildServiceOptions()) {
            presets.add(new OperationDeployPresetItemVo(
                    "restartService:" + option.getKey(),
                    "重启 " + option.getLabel()));
        }
        return presets;
    }

    private List<OperationDeployServiceOptionVo> buildServiceOptions() {
        return deployServiceRegistry.entries().stream()
                .map(entry -> new OperationDeployServiceOptionVo(entry.getKey(), entry.getLabel()))
                .collect(Collectors.toList());
    }
}
