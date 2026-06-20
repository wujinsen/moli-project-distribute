package com.moli.user.center.client.shiro;


import com.moli.user.center.client.UserCenterServer;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.utils.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * 自定义Realm, 实现Shiro安全认证
 */
@Slf4j
public class ShiroRealm extends AuthorizingRealm {

    private UserCenterServer userCenterServer;

    public void setUserCenterServer(UserCenterServer userCenterServer) {
        this.userCenterServer = userCenterServer;
    }

    /**
     * 身份认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String userName = (String) authenticationToken.getPrincipal();
        SysUser user = userCenterServer.getInfoByUserName(userName).getData();
        if (user == null) {
            throw new AuthenticationException();
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new LockedAccountException();
        }

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user,
                user.getPassword(),
                ByteSource.Util.bytes(user.getSalt()),
                getName()
        );
        ShiroUtils.deleteCache(userName, true);
        return authenticationInfo;
    }

    /**
     * 授权权限
     * 用户进行权限验证时候Shiro会去缓存中找,如果查不到数据,会执行这个方法去查权限,并放入缓存中
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }
}
