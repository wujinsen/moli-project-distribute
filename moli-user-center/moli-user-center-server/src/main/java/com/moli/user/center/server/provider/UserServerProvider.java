package com.moli.user.center.server.provider;


import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.User;
import com.moli.user.center.client.UserCenterServer;
import com.moli.user.center.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import javax.annotation.Resource;

@DubboService(version = "1.0.0", group = "moli", protocol = "dubbo")
@Slf4j
public class UserServerProvider implements UserCenterServer {

    @Resource
    private UserService userService;

    @Override
    public MoliResult<User> getInfoByUserName(String userName) {
        return MoliResult.success(userService.getInfoByUserName(userName));
    }
}
