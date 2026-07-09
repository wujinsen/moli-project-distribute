package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.vo.OperationPlatformVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.common.page.PageRes;
import com.moli.user.center.server.operation.service.OperationPlatformService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/platform")
@Api(tags = "运维平台管理")
@Slf4j
public class OperationPlatformController {

    @Resource
    private OperationPlatformService operationPlatformService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.OPERATION_PLATFORM_LIST)
    @ApiOperation(value = "运维平台列表", notes = "运维平台列表")
    public MoliResult<PageRes<OperationPlatformVo>> list(OperationPlatformInfo operationPlatformInfo) {
        return MoliResult.success(operationPlatformService.list(operationPlatformInfo));
    }

    @PostMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PLATFORM_ADD, PermissionConstants.OPERATION_PLATFORM_LIST}, logical = Logical.AND)
    @MoliLog(title = "添加运维平台", businessType = BusinessTypeEnum.INSERT)
    @ApiOperation(value = "添加运维平台", notes = "添加运维平台")
    public MoliResult<Boolean> insert(@RequestBody OperationPlatformInfo operationPlatformInfo) {
        operationPlatformService.create(operationPlatformInfo);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PLATFORM_EDIT, PermissionConstants.OPERATION_PLATFORM_LIST}, logical = Logical.AND)
    @MoliLog(title = "更新运维平台", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "更新运维平台", notes = "更新运维平台")
    public MoliResult<Boolean> update(@RequestBody OperationPlatformInfo operationPlatformInfo) {
        operationPlatformService.update(operationPlatformInfo);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping(value = "/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_PLATFORM_LIST)
    @ApiOperation(value = "查询单个运维平台", notes = "查询单个运维平台")
    public MoliResult<OperationPlatformVo> selectOne(@PathVariable Long id) {
        return MoliResult.success(operationPlatformService.getById(id));
    }

    @GetMapping(value = "/{id}/secret")
    @RequiresPermissions(PermissionConstants.OPERATION_SECRET_VIEW)
    @MoliLog(title = "查看运维平台凭据", businessType = BusinessTypeEnum.OTHER, isSaveResponseData = false)
    @ApiOperation(value = "查看运维平台明文凭据", notes = "需 operation:secret:view 权限")
    public MoliResult<OperationSecretRevealVo> revealSecret(@PathVariable Long id) {
        return MoliResult.success(operationPlatformService.revealPassword(id));
    }

    @DeleteMapping("/{ids}")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PLATFORM_REMOVE, PermissionConstants.OPERATION_PLATFORM_LIST}, logical = Logical.AND)
    @MoliLog(title = "删除运维平台", businessType = BusinessTypeEnum.DELETE)
    @ApiOperation(value = "删除运维平台", notes = "删除运维平台")
    public MoliResult<Boolean> remove(@PathVariable Long[] ids) {
        operationPlatformService.deleteByIds(ids);
        return MoliResult.success(Boolean.TRUE);
    }
}
