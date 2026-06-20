package com.moli.bi.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.shushan.demo.server.mapper"})
@ComponentScan(basePackages = {"com.moli.bi.server.*", "com.moli.user.center.client.*"})
@EnableDiscoveryClient
public class BiApplication {
    public static void main(String[] args) {

        SpringApplication.run(BiApplication.class, args);
    }
}
