package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.dto.operation.OperationProjectSaveRequest;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationProjectComponentLinksVo;
import com.moli.user.center.common.domain.vo.OperationProjectLinksVo;
import com.moli.user.center.common.domain.vo.OperationProjectVo;
import com.moli.common.page.PageRes;
import com.moli.user.center.server.operation.service.OperationProjectComponentLinkService;
import com.moli.user.center.server.operation.service.OperationProjectLinkService;
import com.moli.user.center.server.operation.service.OperationProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/operation/project")
@Api(tags = "项目管理")
@Slf4j
public class OperationProjectController {

    @Resource
    private OperationProjectService operationProjectService;
    @Resource
    private OperationProjectLinkService operationProjectLinkService;
    @Resource
    private OperationProjectComponentLinkService operationProjectComponentLinkService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "项目列表", notes = "项目列表")
    public MoliResult<PageRes<OperationProjectVo>> list(OperationProjectDeployInfo operationProjectDeployInfo) {
        return MoliResult.success(operationProjectService.list(operationProjectDeployInfo));
    }

    @PostMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PROJECT_ADD, PermissionConstants.OPERATION_PROJECT_LIST}, logical = Logical.AND)
    @MoliLog(title = "添加项目", businessType = BusinessTypeEnum.INSERT)
    @ApiOperation(value = "添加项目", notes = "添加项目")
    public MoliResult<Boolean> insert(@Valid @RequestBody OperationProjectSaveRequest request) {
        operationProjectService.create(request);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PROJECT_EDIT, PermissionConstants.OPERATION_PROJECT_LIST}, logical = Logical.AND)
    @MoliLog(title = "更新项目", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "更新项目", notes = "更新项目")
    public MoliResult<Boolean> update(@Valid @RequestBody OperationProjectSaveRequest request) {
        operationProjectService.update(request);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping(value = "/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "查询单个项目", notes = "查询单个项目")
    public MoliResult<OperationProjectVo> selectOne(@PathVariable Long id) {
        return MoliResult.success(operationProjectService.getById(id));
    }

    @DeleteMapping("/{ids}")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PROJECT_REMOVE, PermissionConstants.OPERATION_PROJECT_LIST}, logical = Logical.AND)
    @MoliLog(title = "删除项目", businessType = BusinessTypeEnum.DELETE)
    @ApiOperation(value = "删除项目", notes = "删除项目")
    public MoliResult<Boolean> remove(@PathVariable Long[] ids) {
        operationProjectService.deleteByIds(ids);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping(value = "/{id}/links")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "项目关联服务器", notes = "查询项目关联的服务器 ID 列表")
    public MoliResult<OperationProjectLinksVo> links(@PathVariable Long id) {
        return MoliResult.success(operationProjectLinkService.getLinks(id));
    }

    @PutMapping(value = "/{id}/links")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PROJECT_EDIT, PermissionConstants.OPERATION_PROJECT_LIST}, logical = Logical.AND)
    @MoliLog(title = "项目关联服务器", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "保存项目关联服务器", notes = "全量替换 operation_server_project")
    public MoliResult<Boolean> saveLinks(@PathVariable Long id, @RequestBody OperationProjectLinksVo links) {
        operationProjectLinkService.saveLinks(id, links);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping(value = "/{id}/component-links")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "项目依赖组件", notes = "查询项目关联的组件 ID 列表")
    public MoliResult<OperationProjectComponentLinksVo> componentLinks(@PathVariable Long id) {
        return MoliResult.success(operationProjectComponentLinkService.getLinks(id));
    }

    @PutMapping(value = "/{id}/component-links")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_PROJECT_EDIT, PermissionConstants.OPERATION_PROJECT_LIST}, logical = Logical.AND)
    @MoliLog(title = "项目依赖组件", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "保存项目依赖组件", notes = "全量替换 operation_project_component")
    public MoliResult<Boolean> saveComponentLinks(@PathVariable Long id,
                                                  @RequestBody OperationProjectComponentLinksVo links) {
        operationProjectComponentLinkService.saveLinks(id, links);
        return MoliResult.success(Boolean.TRUE);
    }
}
