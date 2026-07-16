package com.moli.bi.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan(basePackages = {"com.moli.bi.server.mapper"})
@EnableDiscoveryClient
public class BiApplication {
    public static void main(String[] args) {

        SpringApplication.run(BiApplication.class, args);
    }
}
