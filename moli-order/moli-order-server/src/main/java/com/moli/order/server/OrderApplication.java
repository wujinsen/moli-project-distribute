package com.moli.order.server;

import com.moli.user.center.client.UserCenterClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.moli.order.server.mapper"})
@ComponentScan(basePackages = {"com.moli.order.server.*","com.moli.user.center.client.*"})
@EnableDiscoveryClient
@EnableFeignClients(clients = { UserCenterClient.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
