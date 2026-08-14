package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.dto.operation.OperationCommandExecRequest;
import com.moli.user.center.server.operation.service.OperationCommandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 远程 shell 命令 API（SVR-18）。
 */
@RestController
@RequestMapping("/operation/command")
@Api(tags = "远程命令")
@Slf4j
public class OperationCommandController {

    @Resource
    private OperationCommandService operationCommandService;

    @PostMapping("/exec/task")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_COMMAND_EXEC, PermissionConstants.OPERATION_SERVER_LIST},
            logical = Logical.AND)
    @MoliLog(title = "远程执行命令", businessType = BusinessTypeEnum.OTHER, isSaveRequestData = false)
    @ApiOperation(value = "创建远程命令任务", notes = "受控开放 shell；返回 taskId 供轮询")
    public MoliResult<Long> createTask(@Valid @RequestBody OperationCommandExecRequest body) {
        return MoliResult.success(operationCommandService.createCommandTask(
                body.getServerId(), body.getCommand(), body.getWorkDir()));
    }
}
