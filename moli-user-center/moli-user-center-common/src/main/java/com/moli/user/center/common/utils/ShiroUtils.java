package com.moli.user.center.common.utils;

import com.moli.common.utils.SpringUtil;
import com.moli.user.center.common.domain.entity.SysUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.LogoutAware;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisSessionDAO;

import java.util.Collection;
import java.util.Objects;

public final class ShiroUtils {

    private ShiroUtils() {
    }

    public static Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }

    public static void logout() {
        SecurityUtils.getSubject().logout();
    }

    public static SysUser getUserInfo() {
        return (SysUser) SecurityUtils.getSubject().getPrincipal();
    }

    public static void deleteCache(String userName, boolean isRemoveSession) {
        RedisSessionDAO redisSessionDAO = SpringUtil.getBean(RedisSessionDAO.class);
        Session session = null;
        Object attribute = null;
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        SysUser user;
        for (Session sessionInfo : sessions) {
            attribute = sessionInfo.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
            if (attribute == null) {
                continue;
            }
            user = (SysUser) ((SimplePrincipalCollection) attribute).getPrimaryPrincipal();
            if (user == null) {
                continue;
            }
            if (Objects.equals(user.getUserName(), userName)) {
                session = sessionInfo;
                break;
            }
        }
        if (session == null || attribute == null) {
            return;
        }
        if (isRemoveSession) {
            redisSessionDAO.delete(session);
        }
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        Authenticator authc = securityManager.getAuthenticator();
        ((LogoutAware) authc).onLogout((SimplePrincipalCollection) attribute);
    }
}