package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.vo.OperationTopologyGraphVo;
import com.moli.user.center.server.operation.service.OperationTopologyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/topology")
@Api(tags = "运维拓扑图")
@Slf4j
public class OperationTopologyController {

    @Resource
    private OperationTopologyService operationTopologyService;

    @GetMapping
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "全局拓扑图", notes = "一次返回服务器/项目/组件节点与 deploys/depends_on 边")
    public MoliResult<OperationTopologyGraphVo> graph() {
        return MoliResult.success(operationTopologyService.getGraph());
    }
}
