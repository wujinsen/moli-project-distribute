package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ops.secret")
public class OperationSecretProperties {

    /**
     * AES-256-GCM 密钥：32 字节 Base64 或其 UTF-8 字符串（内部 SHA-256 派生）。
     * 环境变量：OPS_SECRET_KEY
     */
    private String key = "";
}
