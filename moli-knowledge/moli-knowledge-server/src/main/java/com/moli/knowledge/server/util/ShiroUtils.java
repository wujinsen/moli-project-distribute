package com.moli.knowledge.server.util;

import com.moli.user.center.common.domain.entity.SysUser;
import org.apache.shiro.SecurityUtils;

public final class ShiroUtils {

    private ShiroUtils() {
    }

    public static SysUser getUserInfo() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal instanceof SysUser) {
            return (SysUser) principal;
        }
        return null;
    }

    public static Long getUserId() {
        SysUser user = getUserInfo();
        return user == null ? null : user.getId();
    }
}
