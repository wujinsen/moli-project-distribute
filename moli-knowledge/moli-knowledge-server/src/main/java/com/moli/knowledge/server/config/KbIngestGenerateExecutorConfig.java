package com.moli.knowledge.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * T15f · Ingest 多页生成异步线程池。
 */
@Configuration
public class KbIngestGenerateExecutorConfig {

    @Bean(name = "ingestGenerateExecutor")
    public Executor ingestGenerateExecutor(KbIngestProperties properties) {
        KbIngestProperties.Generate generate = properties.getGenerate();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(generate.getCorePoolSize());
        executor.setMaxPoolSize(generate.getMaxPoolSize());
        executor.setQueueCapacity(generate.getQueueCapacity());
        executor.setThreadNamePrefix("ingest-generate-");
        executor.initialize();
        return executor;
    }
}
