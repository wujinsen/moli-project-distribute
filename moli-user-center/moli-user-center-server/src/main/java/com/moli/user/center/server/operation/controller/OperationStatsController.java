package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.vo.OperationStatsVo;
import com.moli.user.center.server.operation.service.OperationStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/operation")
@Api(tags = "运维统计")
@Slf4j
public class OperationStatsController {

    @Resource
    private OperationStatsService operationStatsService;

    @GetMapping("/stats")
    @RequiresPermissions(PermissionConstants.OPERATION_PROJECT_LIST)
    @ApiOperation(value = "运维资产统计", notes = "驾驶舱 ops 页与台账汇总")
    public MoliResult<OperationStatsVo> stats() {
        return MoliResult.success(operationStatsService.getStats());
    }
}
