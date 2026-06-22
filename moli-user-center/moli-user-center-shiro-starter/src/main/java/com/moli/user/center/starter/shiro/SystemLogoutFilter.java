package com.moli.user.center.starter.shiro;

import com.alibaba.fastjson.JSON;
import com.moli.common.core.MoliResult;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class SystemLogoutFilter extends LogoutFilter {

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);
        subject.logout();
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.getWriter().write(JSON.toJSONString(MoliResult.success()));
        return false;
    }
}
