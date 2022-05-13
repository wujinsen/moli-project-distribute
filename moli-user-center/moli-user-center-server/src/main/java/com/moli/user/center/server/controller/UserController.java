package com.moli.user.center.server.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @GetMapping("/aaa")
    @SentinelResource("aaa")
    public String aaa(){
        System.out.println("user aaa");
        return "user aaa";
    }

    @GetMapping("/test")
    public String test1() {
        return "Hello test";
    }

}
