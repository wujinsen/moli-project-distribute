package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.service.OperationDeployService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation/deploy")
@Api(tags = "部署脚本")
@Slf4j
public class OperationDeployController {

    @Resource
    private OperationDeployService operationDeployService;

    @GetMapping("/{serviceKey}/status")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "查询服务进程状态", notes = "只读调用 deploy/linux/moli-service.sh status")
    public MoliResult<OperationDeployStatusVo> status(@PathVariable String serviceKey) {
        return MoliResult.success(operationDeployService.status(serviceKey));
    }

    @PostMapping("/{serviceKey}/{action}")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_DEPLOY_EXEC, PermissionConstants.OPERATION_SERVER_LIST},
            logical = Logical.AND)
    @MoliLog(title = "部署脚本动作", businessType = BusinessTypeEnum.OTHER)
    @ApiOperation(value = "执行部署脚本", notes = "status/logs 只读；start/stop/restart 需 ops.deploy.enabled=true")
    public MoliResult<OperationDeployStatusVo> execute(
            @PathVariable String serviceKey,
            @PathVariable String action,
            @RequestParam(required = false) String arg) {
        return MoliResult.success(operationDeployService.execute(serviceKey, action, arg));
    }
}
