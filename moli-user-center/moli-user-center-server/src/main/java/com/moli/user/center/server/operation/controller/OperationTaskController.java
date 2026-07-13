package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.vo.OperationTaskProjectGroupVo;
import com.moli.user.center.common.domain.vo.OperationTaskVo;
import com.moli.user.center.server.operation.service.OperationTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 运维异步任务轮询 API（SVR-14）。
 */
@RestController
@RequestMapping("/operation/task")
@Api(tags = "运维任务")
@Slf4j
public class OperationTaskController {

    @Resource
    private OperationTaskService operationTaskService;

    @GetMapping("/{id}")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "轮询任务", notes = "返回状态/进度 + 从 logOffset 起的增量日志")
    public MoliResult<OperationTaskVo> poll(@PathVariable Long id,
                                            @RequestParam(defaultValue = "0") Integer logOffset) {
        return MoliResult.success(operationTaskService.poll(id, logOffset));
    }

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "任务历史", notes = "按类型/服务器/项目过滤，不含日志大字段")
    public MoliResult<PageRes<OperationTaskVo>> list(
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return MoliResult.success(operationTaskService.list(taskType, serverId, projectId, pageNum, pageSize));
    }

    @GetMapping("/groups")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @ApiOperation(value = "任务历史按项目分组", notes = "分页在项目组维度；组内含最近 tasksPerGroup 条任务")
    public MoliResult<PageRes<OperationTaskProjectGroupVo>> groups(
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "20") Integer tasksPerGroup) {
        return MoliResult.success(operationTaskService.listGroups(
                taskType, serverId, projectId, status, pageNum, pageSize, tasksPerGroup));
    }

    @PostMapping("/{id}/cancel")
    @RequiresPermissions(PermissionConstants.OPERATION_SERVER_LIST)
    @MoliLog(title = "取消运维任务", businessType = BusinessTypeEnum.OTHER)
    @ApiOperation(value = "取消任务", notes = "pending/running 协作式取消；success/failed/cancelled 返回错误")
    public MoliResult<OperationTaskVo> cancel(@PathVariable Long id) {
        return MoliResult.success(operationTaskService.cancel(id));
    }
}
