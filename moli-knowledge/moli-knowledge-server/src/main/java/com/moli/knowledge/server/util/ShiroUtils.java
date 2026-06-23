package com.moli.knowledge.server.util;

import com.moli.user.center.common.domain.entity.SysUser;
import org.apache.shiro.SecurityUtils;

public final class ShiroUtils {

    private ShiroUtils() {
    }

    public static SysUser getUserInfo() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof SysUser) {
                return (SysUser) principal;
            }
        } catch (Exception ignored) {
            // Shiro 未初始化或未登录
        }
        return null;
    }

    public static Long getUserId() {
        SysUser user = getUserInfo();
        return user == null ? null : user.getId();
    }
}
