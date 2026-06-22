package com.moli.user.center.starter.shiro;

import com.alibaba.fastjson.JSON;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.ResponseCodeEnums;
import com.moli.user.center.api.UserCenterServer;
import com.moli.user.center.common.domain.entity.SysUser;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends FormAuthenticationFilter {

    static final String AUTH_DENIED_CODE_ATTR = "shiroAuthDeniedCode";
    static final String AUTH_DENIED_MSG_ATTR = "shiroAuthDeniedMsg";

    private UserCenterServer userCenterServer;

    public void setUserCenterServer(UserCenterServer userCenterServer) {
        this.userCenterServer = userCenterServer;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
            throws Exception {
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setStatus(HttpStatus.OK.value());
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
        httpServletResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        Object codeAttr = request.getAttribute(AUTH_DENIED_CODE_ATTR);
        Object msgAttr = request.getAttribute(AUTH_DENIED_MSG_ATTR);
        int code = codeAttr instanceof Integer
                ? (Integer) codeAttr
                : ResponseCodeEnums.TOKEN_INVALID_ERROR_CODE.getCode();
        String msg = msgAttr instanceof String
                ? (String) msgAttr
                : ResponseCodeEnums.TOKEN_INVALID_ERROR_CODE.getMessage();
        httpServletResponse.getWriter().write(JSON.toJSONString(MoliResult.errorMsg(code, msg)));
        return false;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (request instanceof HttpServletRequest
                && ((HttpServletRequest) request).getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }
        if (!super.isAccessAllowed(request, response, mappedValue)) {
            return false;
        }
        Subject subject = getSubject(request, response);
        Object principal = subject.getPrincipal();
        if (!(principal instanceof SysUser) || userCenterServer == null) {
            return true;
        }
        SysUser sessionUser = (SysUser) principal;
        if (sessionUser.getId() == null) {
            return true;
        }
        MoliResult<SysUser> result = userCenterServer.getUserById(sessionUser.getId());
        SysUser latest = result == null ? null : result.getData();
        if (latest == null
                || CommonConstant.IS_DELETE.equals(latest.getIsDelete())
                || (latest.getStatus() != null && latest.getStatus() == 0)) {
            subject.logout();
            request.setAttribute(AUTH_DENIED_CODE_ATTR, ResponseCodeEnums.TOKEN_INVALID_ERROR_CODE.getCode());
            request.setAttribute(AUTH_DENIED_MSG_ATTR, "Account disabled, please login again");
            return false;
        }
        return true;
    }
}
