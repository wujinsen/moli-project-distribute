package com.moli.user.center.server.operation.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.BusinessTypeEnum;
import com.moli.common.log.MoliLog;
import com.moli.user.center.server.operation.service.OperationFileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 文件上传发布 API（SVR-16）。
 */
@RestController
@RequestMapping("/operation/file")
@Api(tags = "文件上传发布")
@Slf4j
public class OperationFileController {

    @Resource
    private OperationFileUploadService operationFileUploadService;

    @PostMapping("/upload")
    @RequiresPermissions(value = {PermissionConstants.OPERATION_FILE_UPLOAD, PermissionConstants.OPERATION_SERVER_LIST},
            logical = Logical.AND)
    @MoliLog(title = "上传文件到远程服务器", businessType = BusinessTypeEnum.OTHER, isSaveRequestData = false)
    @ApiOperation(value = "上传文件发布", notes = "SFTP 上传到白名单路径 + 后置动作（none/nginxReload/unzipToDist/restartService:{key}）；返回 taskId 供轮询")
    public MoliResult<Long> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long serverId,
            @RequestParam String targetPath,
            @RequestParam(required = false, defaultValue = "none") String postAction) {
        return MoliResult.success(
                operationFileUploadService.createUploadTask(file, serverId, targetPath, postAction));
    }
}
