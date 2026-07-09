package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.vo.OperationPortAuditVo;
import com.moli.user.center.server.operation.service.OperationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/audit")
@Api(tags = "运维审计")
@Slf4j
public class OperationAuditController {

    @Resource
    private OperationAuditService operationAuditService;

    @GetMapping("/port-matrix")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "端口矩阵校验", notes = "对照 production-checklist 端口表校验项目/组件台账")
    public MoliResult<OperationPortAuditVo> portMatrix() {
        return MoliResult.success(operationAuditService.auditPortMatrix());
    }
}
