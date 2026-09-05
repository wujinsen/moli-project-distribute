package com.moli.common.web;

import com.moli.common.core.MoliResult;
import com.moli.common.core.TraceIds;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 管理接口 / 错误信封统一补 {@code traceId}（32 位根 ID）。
 * 控制器手写 {@code new MoliResult} 时工厂方法可能没走到，这里兜底。
 */
@ControllerAdvice
public class MoliResultTraceAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        return MoliResult.class.isAssignableFrom(type);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body instanceof MoliResult) {
            MoliResult<?> result = (MoliResult<?>) body;
            if (result.getTraceId() == null || result.getTraceId().isEmpty()) {
                result.setTraceId(TraceIds.currentRoot());
            }
        }
        return body;
    }
}
