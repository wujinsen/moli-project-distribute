package com.moli.user.center.server.operation.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传发布（SVR-16/19）：SFTP 上传 + 快捷预设或自定义 shell 后置。
 */
public interface OperationFileUploadService {

    /**
     * 创建异步上传任务。
     *
     * @param postAction  none / nginxReload / unzipToDist / restartService:{key} / custom
     * @param postCommand postAction=custom 时的 shell 命令
     */
    Long createUploadTask(MultipartFile file, Long serverId, String targetPath,
                          String postAction, String postCommand);
}
