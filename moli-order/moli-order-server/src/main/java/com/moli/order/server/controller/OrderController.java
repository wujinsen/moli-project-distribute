package com.moli.order.server.controller;


import com.moli.user.center.api.UserCenterServer;
import com.moli.user.center.common.domain.entity.SysUser;
import io.swagger.annotations.Api;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
@Api(tags = "订单管理")
public class OrderController {

    @DubboReference(version = "1.0.0", group = "moli", protocol = "dubbo", check = false)
    private UserCenterServer userCenterServer;

//    @GetMapping("/aaa")
//    public String aaa() {
////        if(1==1){
////            throw new BaseException("111", "出错啦");
////        }
//        System.out.println("order aaa");
//
//     System.out.println("request usercenter: " + userCenterClient.getInfoByUserName("admin"));
//
//        return "order aaa";
//    }
//
//    @GetMapping("bbb")
//    public String bbb() {
//
//        System.out.println("order bbb");
//
//        System.out.println("request usercenter: " + userCenterServer.getInfoByUserName("admin"));
//        return "order bbb";
//    }

    @GetMapping("ccc")
    public Object getUserInfo() {
        //  ShiroUtils.getUserInfo();
        System.out.println("order ccc");
        return (SysUser) SecurityUtils.getSubject().getPrincipal();
    }

}
