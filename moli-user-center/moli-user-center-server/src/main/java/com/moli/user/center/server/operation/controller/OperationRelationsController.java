package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.vo.OperationRelationsVo;
import com.moli.user.center.server.operation.service.OperationRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/relations")
@Api(tags = "运维关联关系")
@Slf4j
public class OperationRelationsController {

    @Resource
    private OperationRelationService operationRelationService;

    @GetMapping("/{entityType}/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "实体关联详情", notes = "entityType=server|project|component，返回关联服务器/项目/组件与最近任务")
    public MoliResult<OperationRelationsVo> relations(@PathVariable String entityType, @PathVariable Long id) {
        return MoliResult.success(operationRelationService.getRelations(entityType, id));
    }
}
