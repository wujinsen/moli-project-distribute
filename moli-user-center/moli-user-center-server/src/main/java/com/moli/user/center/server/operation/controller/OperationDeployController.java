package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.dto.operation.OperationDeployBatchTaskRequest;
import com.moli.user.center.common.domain.vo.OperationDeployPresetsVo;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.support.OperationDeployLocalPolicy;
import com.moli.user.center.server.operation.support.OperationDtoValidationSupport;
import com.moli.user.center.server.operation.service.OperationDeployPresetService;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/operation/deploy")
@Api(tags = "部署脚本")
@Slf4j
public class OperationDeployController {

    @Resource
    private OperationDeployService operationDeployService;
    @Resource
    private OperationRemoteDeployService operationRemoteDeployService;
    @Resource
    private OperationDeployPresetService operationDeployPresetService;
    @Resource
    private OperationDtoValidationSupport dtoValidation;
    @Resource
    private OperationDeployLocalPolicy deployLocalPolicy;

    @GetMapping("/presets")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "部署中心预设", notes = "常用上传路径与快捷后置动作")
    public MoliResult<OperationDeployPresetsVo> presets(@RequestParam(required = false) Long serverId) {
        return MoliResult.success(operationDeployPresetService.getPresets(serverId));
    }

    @GetMapping("/{serviceKey}/status")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "查询服务进程状态", notes = "serverId 非空时 SSH 远程 status；为空时需 ops.deploy.allow-local=true 才查本机")
    public MoliResult<OperationDeployStatusVo> status(
            @PathVariable String serviceKey,
            @RequestParam(required = false) Long serverId) {
        if (serverId != null) {
            return MoliResult.success(
                    operationRemoteDeployService.executeRemoteReadOnly(serverId, serviceKey, "status", null));
        }
        deployLocalPolicy.requireAllowLocal();
        return MoliResult.success(operationDeployService.status(serviceKey));
    }

    @PostMapping("/{serviceKey}/{action}")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_DEPLOY_EXEC, PermissionConstants.OPERATION_SERVER_LIST},
            logical = Logical.AND)
    @MoliLog(title = "部署脚本动作", businessType = BusinessTypeEnum.OTHER)
    @ApiOperation(value = "执行部署脚本（同步）", notes = "serverId 非空走 SSH 只读；为空需 allow-local=true 才本机执行")
    public MoliResult<OperationDeployStatusVo> execute(
            @PathVariable String serviceKey,
            @PathVariable String action,
            @RequestParam(required = false) String arg,
            @RequestParam(required = false) Long serverId) {
        if (serverId != null) {
            return MoliResult.success(
                    operationRemoteDeployService.executeRemoteReadOnly(serverId, serviceKey, action, arg));
        }
        deployLocalPolicy.requireAllowLocal();
        return MoliResult.success(operationDeployService.execute(serviceKey, action, arg));
    }

    @PostMapping("/{serviceKey}/{action}/task")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_DEPLOY_EXEC, PermissionConstants.OPERATION_SERVER_LIST},
            logical = Logical.AND)
    @MoliLog(title = "创建部署任务", businessType = BusinessTypeEnum.OTHER)
    @ApiOperation(value = "创建异步启停任务", notes = "serverId 为空需 allow-local=true；可选 projectId 关联项目台账；返回 taskId")
    public MoliResult<Long> createTask(
            @PathVariable String serviceKey,
            @PathVariable String action,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long projectId) {
        return MoliResult.success(operationRemoteDeployService.createDeployTask(
                dtoValidation.deployTask(serviceKey, action, serverId, projectId)));
    }

    @PostMapping("/batch/task")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_DEPLOY_EXEC, PermissionConstants.OPERATION_SERVER_LIST},
            logical = Logical.AND)
    @MoliLog(title = "创建批量滚动重启任务", businessType = BusinessTypeEnum.OTHER)
    @ApiOperation(value = "创建批量滚动重启任务", notes = "单父任务顺序执行 steps；返回 taskId，轮询 GET /operation/task/{id}")
    public MoliResult<Long> createBatchTask(@Valid @RequestBody OperationDeployBatchTaskRequest request) {
        return MoliResult.success(operationRemoteDeployService.createBatchDeployTask(dtoValidation.validate(request)));
    }
}
