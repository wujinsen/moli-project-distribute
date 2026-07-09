package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.vo.OperationComponentVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.common.page.PageRes;
import com.moli.user.center.server.operation.service.OperationComponentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/component")
@Api(tags = "运维组件管理")
@Slf4j
public class OperationComponentController {

    @Resource
    private OperationComponentService operationComponentService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.OPERATION_COMPONENT_LIST)
    @ApiOperation(value = "组件列表", notes = "组件列表")
    public MoliResult<PageRes<OperationComponentVo>> list(OperationComponentDeployInfo operationComponentDeployInfo) {
        return MoliResult.success(operationComponentService.list(operationComponentDeployInfo));
    }

    @PostMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_COMPONENT_ADD, PermissionConstants.OPERATION_COMPONENT_LIST}, logical = Logical.AND)
    @MoliLog(title = "添加运维组件", businessType = BusinessTypeEnum.INSERT)
    @ApiOperation(value = "添加组件", notes = "添加组件")
    public MoliResult<Boolean> insert(@RequestBody OperationComponentDeployInfo operationComponentDeployInfo) {
        operationComponentService.create(operationComponentDeployInfo);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_COMPONENT_EDIT, PermissionConstants.OPERATION_COMPONENT_LIST}, logical = Logical.AND)
    @MoliLog(title = "更新运维组件", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "更新组件", notes = "更新组件")
    public MoliResult<Boolean> update(@RequestBody OperationComponentDeployInfo operationComponentDeployInfo) {
        operationComponentService.update(operationComponentDeployInfo);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping(value = "/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_COMPONENT_LIST)
    @ApiOperation(value = "查询单个组件", notes = "查询单个组件")
    public MoliResult<OperationComponentVo> selectOne(@PathVariable Long id) {
        return MoliResult.success(operationComponentService.getById(id));
    }

    @GetMapping(value = "/{id}/secret")
    @RequiresPermissions(PermissionConstants.OPERATION_SECRET_VIEW)
    @MoliLog(title = "查看运维组件凭据", businessType = BusinessTypeEnum.OTHER, isSaveResponseData = false)
    @ApiOperation(value = "查看运维组件明文凭据", notes = "需 operation:secret:view 权限")
    public MoliResult<OperationSecretRevealVo> revealSecret(@PathVariable Long id) {
        return MoliResult.success(operationComponentService.revealPassword(id));
    }

    @PostMapping(value = "/{id}/check")
    @RequiresPermissions(PermissionConstants.OPERATION_COMPONENT_LIST)
    @MoliLog(title = "探测组件健康", businessType = BusinessTypeEnum.OTHER, isSaveRequestData = false)
    @ApiOperation(value = "探测组件健康", notes = "TCP 端口探活并更新 status")
    public MoliResult<OperationComponentVo> checkHealth(@PathVariable Long id) {
        return MoliResult.success(operationComponentService.checkHealth(id));
    }

    @DeleteMapping("/{ids}")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_COMPONENT_REMOVE, PermissionConstants.OPERATION_COMPONENT_LIST}, logical = Logical.AND)
    @MoliLog(title = "删除运维组件", businessType = BusinessTypeEnum.DELETE)
    @ApiOperation(value = "删除组件", notes = "删除组件")
    public MoliResult<Boolean> remove(@PathVariable Long[] ids) {
        operationComponentService.deleteByIds(ids);
        return MoliResult.success(Boolean.TRUE);
    }
}
