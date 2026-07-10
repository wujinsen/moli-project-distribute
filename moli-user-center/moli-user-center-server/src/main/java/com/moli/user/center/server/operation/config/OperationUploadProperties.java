package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传发布参数（SVR-16）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ops.upload")
public class OperationUploadProperties {

    /** 是否允许通过系统上传文件到远程服务器。 */
    private boolean enabled = false;

    /** 本地暂存目录；空则用系统临时目录。 */
    private String tempDir = "";

    /** 单文件大小上限（字节），默认 200MB。 */
    private long maxBytes = 200L * 1024 * 1024;

    /** 远端目标路径白名单前缀（逗号分隔注入）。 */
    private List<String> allowedPaths = new ArrayList<>(Arrays.asList(
            "/opt/moli/frontend/", "/opt/moli-project-distribute/"));

    /** 更宽但仍受限的根目录（路径须在其下），如 /opt,/home/ubuntu */
    private List<String> allowAnyUnder = new ArrayList<>(Arrays.asList("/opt/", "/home/ubuntu/"));
}
