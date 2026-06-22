package com.moli.knowledge.server.config;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, MinioException {
        if (StringUtils.isAnyBlank(properties.getUrl(), properties.getAccessKey(), properties.getSecretKey())) {
            throw new IllegalStateException("MinIO 配置不完整，请检查 minio.url / accessKey / secretKey");
        }
        MinioClient client = new MinioClient(
                properties.getUrl(),
                properties.getAccessKey(),
                properties.getSecretKey()
        );
        ensureBucket(client, properties.getBucket());
        return client;
    }

    private void ensureBucket(MinioClient client, String bucket)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, MinioException {
        if (StringUtils.isBlank(bucket)) {
            throw new IllegalStateException("MinIO bucket 未配置");
        }
        if (!client.bucketExists(bucket)) {
            client.makeBucket(bucket);
            log.info("MinIO bucket created: {}", bucket);
        }
    }
}
