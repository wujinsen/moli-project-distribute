package com.moli.user.center.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.user.center.common.domain.entity.SysUser;

/**
 * 超级管理员（superadmin）与特殊管理员（admin）的可见性与访问控制。
 */
public final class PrivilegedUserUtils {

    private PrivilegedUserUtils() {
    }

    public static boolean isPrivilegedAccount(String userName) {
        return CommonConstant.isSuperAdmin(userName);
    }

    public static boolean isSuperAdminAccount(String userName) {
        return CommonConstant.SUPER_ADMIN.equals(userName);
    }

    public static boolean hasFullPermission(String userName) {
        return CommonConstant.hasFullPermission(userName);
    }

    public static void applyListVisibilityFilter(LambdaQueryWrapper<SysUser> wrapper, String currentUserName) {
        if (!isPrivilegedAccount(currentUserName)) {
            wrapper.and(w -> w.ne(SysUser::getUserName, CommonConstant.SUPER_ADMIN)
                    .ne(SysUser::getUserName, CommonConstant.LEGACY_SUPER_ADMIN));
        }
    }

    public static boolean canViewUser(SysUser target, SysUser current) {
        if (target == null) {
            return false;
        }
        if (!isPrivilegedAccount(target.getUserName())) {
            return true;
        }
        return current != null && isPrivilegedAccount(current.getUserName());
    }
}
