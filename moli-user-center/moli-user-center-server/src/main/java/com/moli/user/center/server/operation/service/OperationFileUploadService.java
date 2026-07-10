package com.moli.user.center.server.operation.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传发布（SVR-16）：SFTP 上传 + 白名单后置动作。
 */
public interface OperationFileUploadService {

    /**
     * 创建异步上传任务。
     * @param serverId   目标服务器（必填）
     * @param targetPath 远端目标路径（白名单前缀内；以 / 结尾时拼接原文件名）
     * @param postAction none / nginxReload / unzipToDist / restartService:{serviceKey}
     * @return taskId
     */
    Long createUploadTask(MultipartFile file, Long serverId, String targetPath, String postAction);
}
