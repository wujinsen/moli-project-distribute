package com.moli.knowledge.server.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        if (StringUtils.isAnyBlank(properties.getUrl(), properties.getAccessKey(), properties.getSecretKey())) {
            throw new IllegalStateException("MinIO 配置不完整，请检查 minio.url / accessKey / secretKey");
        }
        try {
            return new MinioClient(
                    properties.getUrl(),
                    properties.getAccessKey(),
                    properties.getSecretKey()
            );
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 客户端初始化失败: " + properties.getUrl(), e);
        }
    }
}
