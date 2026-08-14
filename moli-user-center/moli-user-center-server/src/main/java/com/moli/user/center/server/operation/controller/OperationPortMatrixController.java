package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationPortMatrixSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.common.domain.vo.OperationPortMatrixVo;
import com.moli.user.center.server.operation.service.OperationPortMatrixService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/operation/port-matrix")
@Api(tags = "端口矩阵管理")
@Slf4j
public class OperationPortMatrixController {

    @Resource
    private OperationPortMatrixService operationPortMatrixService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.OPERATION_PORT_MATRIX_LIST)
    @ApiOperation(value = "端口矩阵列表", notes = "分页列表")
    public MoliResult<PageRes<OperationPortMatrixVo>> list(OperationPortMatrixInfo query) {
        return MoliResult.success(operationPortMatrixService.list(query));
    }

    @GetMapping("/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_PORT_MATRIX_LIST)
    @ApiOperation(value = "端口矩阵详情", notes = "单条详情")
    public MoliResult<OperationPortMatrixVo> selectOne(@PathVariable Long id) {
        return MoliResult.success(operationPortMatrixService.getById(id));
    }

    @PostMapping
    @RequiresPermissions(value = {
            PermissionConstants.OPERATION_PORT_MATRIX_ADD,
            PermissionConstants.OPERATION_PORT_MATRIX_LIST
    }, logical = Logical.AND)
    @MoliLog(title = "新增端口矩阵", businessType = BusinessTypeEnum.INSERT)
    @ApiOperation(value = "新增端口矩阵", notes = "新增")
    public MoliResult<Boolean> insert(@Valid @RequestBody OperationPortMatrixSaveRequest request) {
        operationPortMatrixService.create(request);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping
    @RequiresPermissions(value = {
            PermissionConstants.OPERATION_PORT_MATRIX_EDIT,
            PermissionConstants.OPERATION_PORT_MATRIX_LIST
    }, logical = Logical.AND)
    @MoliLog(title = "更新端口矩阵", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "更新端口矩阵", notes = "更新")
    public MoliResult<Boolean> update(@Valid @RequestBody OperationPortMatrixSaveRequest request) {
        operationPortMatrixService.update(request);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{ids}")
    @RequiresPermissions(value = {
            PermissionConstants.OPERATION_PORT_MATRIX_REMOVE,
            PermissionConstants.OPERATION_PORT_MATRIX_LIST
    }, logical = Logical.AND)
    @MoliLog(title = "删除端口矩阵", businessType = BusinessTypeEnum.DELETE)
    @ApiOperation(value = "删除端口矩阵", notes = "批量删除")
    public MoliResult<Boolean> remove(@PathVariable Long[] ids) {
        operationPortMatrixService.deleteByIds(ids);
        return MoliResult.success(Boolean.TRUE);
    }
}
