package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;
import com.moli.user.center.server.operation.service.OperationHealthProbeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/health")
@Api(tags = "运维健康探测")
@Slf4j
public class OperationHealthController {

    @Resource
    private OperationHealthProbeService operationHealthProbeService;

    @PostMapping("/probe-all")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @MoliLog(title = "批量运维探活", businessType = BusinessTypeEnum.OTHER, isSaveRequestData = false)
    @ApiOperation(value = "批量探活", notes = "探测全部服务器/组件，同步项目 serverId 与部署进程状态")
    public MoliResult<OperationHealthProbeResultVo> probeAll() {
        return MoliResult.success(operationHealthProbeService.probeAll());
    }
}
