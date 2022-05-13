package com.moli.user.center.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@FeignClient(name = "user-center-server")
public interface UserCenterClient {

    /**
     * 获取所有用户
     * @return
     */
    @GetMapping(path = "/user/aaa")
    String aaa();

}

