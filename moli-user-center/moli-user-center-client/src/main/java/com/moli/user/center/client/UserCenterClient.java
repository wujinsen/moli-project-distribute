package com.moli.user.center.client;


import com.moli.common.core.MoliResult;
import com.moli.common.domain.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "user-center-server")
public interface UserCenterClient {

    /**
     * 根据用户名获取用户
     * @return
     */
    @GetMapping(path = "/user/getInfoByUserName/{userName}")
    MoliResult<User> getInfoByUserName(@PathVariable("userName") String userName);


}

