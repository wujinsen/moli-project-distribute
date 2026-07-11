package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.dto.operation.OperationServerSaveRequest;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationServerInfoVo;
import com.moli.user.center.common.domain.vo.OperationServerLinksVo;
import com.moli.user.center.common.domain.vo.OperationServerSshVo;
import com.moli.user.center.common.domain.vo.OperationServerTopologyVo;
import com.moli.user.center.common.domain.vo.OperationServerVo;
import com.moli.user.center.common.domain.vo.OperationSshTestVo;
import com.moli.common.page.PageRes;
import com.moli.user.center.server.operation.service.OperationServerLinkService;
import com.moli.user.center.server.operation.service.OperationServerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/operation/server")
@Api(tags = "服务器管理")
@Slf4j
public class OperationServerController {

    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationServerLinkService operationServerLinkService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "服务器列表", notes = "服务器列表")
    public MoliResult<PageRes<OperationServerVo>> list(OperationServerInfoVo operationServerInfoVo) {
        return MoliResult.success(operationServerService.list(operationServerInfoVo));
    }

    @PostMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_SERVER_ADD, PermissionConstants.OPERATION_SERVER_LIST}, logical = Logical.AND)
    @MoliLog(title = "添加服务器", businessType = BusinessTypeEnum.INSERT)
    @ApiOperation(value = "添加服务器", notes = "添加服务器")
    public MoliResult<Boolean> insert(@Valid @RequestBody OperationServerSaveRequest request) {
        operationServerService.create(request);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping
    @RequiresPermissions(value = {PermissionConstants.OPERATION_SERVER_EDIT, PermissionConstants.OPERATION_SERVER_LIST}, logical = Logical.AND)
    @MoliLog(title = "更新服务器", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "更新服务器", notes = "更新服务器")
    public MoliResult<Boolean> update(@Valid @RequestBody OperationServerSaveRequest request) {
        operationServerService.update(request);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping(value = "/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "查询单个服务器", notes = "查询单个服务器")
    public MoliResult<OperationServerVo> selectOne(@PathVariable Long id) {
        return MoliResult.success(operationServerService.getById(id));
    }

    @GetMapping(value = "/{id}/topology")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "服务器拓扑", notes = "聚合该服务器上的项目与组件")
    public MoliResult<OperationServerTopologyVo> topology(@PathVariable Long id) {
        return MoliResult.success(operationServerService.getTopology(id));
    }

    @PostMapping(value = "/{id}/check")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @MoliLog(title = "探测服务器健康", businessType = BusinessTypeEnum.OTHER, isSaveRequestData = false)
    @ApiOperation(value = "探测服务器健康", notes = "TCP 端口探活并更新 status")
    public MoliResult<OperationServerVo> checkHealth(@PathVariable Long id) {
        return MoliResult.success(operationServerService.checkHealth(id));
    }

    @GetMapping(value = "/{id}/links")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "服务器关联", notes = "查询 N:N 关联的项目与组件 ID")
    public MoliResult<OperationServerLinksVo> links(@PathVariable Long id) {
        return MoliResult.success(operationServerLinkService.getLinks(id));
    }

    @PutMapping(value = "/{id}/links")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_SERVER_EDIT, PermissionConstants.OPERATION_SERVER_LIST}, logical = Logical.AND)
    @MoliLog(title = "保存服务器关联", businessType = BusinessTypeEnum.UPDATE)
    @ApiOperation(value = "保存服务器关联", notes = "全量替换该服务器关联的项目与组件")
    public MoliResult<Boolean> saveLinks(@PathVariable Long id, @RequestBody OperationServerLinksVo links) {
        operationServerLinkService.saveLinks(id, links);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping(value = "/{id}/ssh")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_SSH_MANAGE, PermissionConstants.OPERATION_SERVER_LIST}, logical = Logical.AND)
    @MoliLog(title = "配置服务器SSH凭据", businessType = BusinessTypeEnum.UPDATE, isSaveRequestData = false)
    @ApiOperation(value = "配置 SSH 凭据", notes = "上传私钥/密码（加密存储、只写不读）；留空表示不修改")
    public MoliResult<Boolean> saveSsh(@PathVariable Long id, @RequestBody OperationServerSshVo form) {
        operationServerService.saveSsh(id, form);
        return MoliResult.success(Boolean.TRUE);
    }

    @PostMapping(value = "/{id}/ssh/test")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_SSH_MANAGE, PermissionConstants.OPERATION_SERVER_LIST}, logical = Logical.AND)
    @MoliLog(title = "测试服务器SSH连接", businessType = BusinessTypeEnum.OTHER, isSaveResponseData = false)
    @ApiOperation(value = "测试 SSH 连接", notes = "使用已保存凭据连接并执行 whoami")
    public MoliResult<OperationSshTestVo> testSsh(@PathVariable Long id) {
        return MoliResult.success(operationServerService.testSsh(id));
    }

    @DeleteMapping("/{ids}")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_SERVER_REMOVE, PermissionConstants.OPERATION_SERVER_LIST}, logical = Logical.AND)
    @MoliLog(title = "删除服务器", businessType = BusinessTypeEnum.DELETE)
    @ApiOperation(value = "删除服务器", notes = "删除服务器")
    public MoliResult<Boolean> remove(@PathVariable Long[] ids) {
        operationServerService.deleteByIds(ids);
        return MoliResult.success(Boolean.TRUE);
    }
}
