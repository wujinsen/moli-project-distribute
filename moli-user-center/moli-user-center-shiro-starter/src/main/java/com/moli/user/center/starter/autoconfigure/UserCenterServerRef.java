package com.moli.user.center.starter.autoconfigure;

import com.moli.user.center.api.UserCenterServer;
import org.apache.dubbo.config.annotation.DubboReference;

/**
 * Dubbo 引用独立成 Bean，避免 {@code @DubboReference} 写在 {@code @Configuration} 上
 * 与 Shiro {@code securityManager} 形成循环依赖。
 */
public class UserCenterServerRef {

    @DubboReference(version = "1.0.0", group = "moli", protocol = "dubbo", check = false)
    private UserCenterServer userCenterServer;

    public UserCenterServer get() {
        return userCenterServer;
    }
}
