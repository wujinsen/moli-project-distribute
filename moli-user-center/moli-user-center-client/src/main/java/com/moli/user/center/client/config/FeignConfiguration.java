package com.moli.user.center.client.config;

import com.moli.user.center.client.shiro.ShiroSessionManager;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 服务间相互调用传递token
 */
@Configuration
public class FeignConfiguration implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        requestTemplate.header(ShiroSessionManager.AUTHORIZATION, request.getHeader(ShiroSessionManager.AUTHORIZATION));
    }
}
