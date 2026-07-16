package com.moli.ai.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan(basePackages = {"com.moli.ai.server.mapper"})
@EnableDiscoveryClient
public class AiApplication {
    public static void main(String[] args) {

        SpringApplication.run(AiApplication.class, args);
    }
}
