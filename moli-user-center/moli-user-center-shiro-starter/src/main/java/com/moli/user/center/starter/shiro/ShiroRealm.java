package com.moli.user.center.starter.shiro;

import com.moli.common.core.MoliResult;
import com.moli.user.center.api.UserCenterServer;
import com.moli.user.center.common.domain.entity.SysUser;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

public class ShiroRealm extends AuthorizingRealm {

    private static final String LOGIN_VIA_USER_CENTER_MSG =
            "Login via user-center only (POST /UserCenter/login)";

    private UserCenterServer userCenterServer;

    public void setUserCenterServer(UserCenterServer userCenterServer) {
        this.userCenterServer = userCenterServer;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        throw new AuthenticationException(LOGIN_VIA_USER_CENTER_MSG);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        SysUser user = (SysUser) principalCollection.getPrimaryPrincipal();
        if (user == null || userCenterServer == null) {
            return authorizationInfo;
        }
        MoliResult<Set<String>> result = userCenterServer.getPermissionsByUserId(user.getId(), user.getUserName());
        if (result != null && result.getData() != null) {
            authorizationInfo.setStringPermissions(result.getData());
        }
        return authorizationInfo;
    }
}
