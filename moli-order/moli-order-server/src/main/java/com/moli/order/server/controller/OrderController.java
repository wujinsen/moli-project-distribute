package com.moli.order.server.controller;


import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
@Api(tags = "订单管理")
public class OrderController {

//    @Resource
//    private UserCenterClient userCenterClient;

    @GetMapping("/aaa")
    public String aaa() {
//        if(1==1){
//            throw new BaseException("111", "出错啦");
//        }
        System.out.println("order aaa");

     //  System.out.println("request usercenter: " + userCenterClient.aaa());

        return "order aaa";
    }

}
