package com.moli.common.autoconfigure;

import com.moli.common.core.MoliResult;
import com.moli.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 公共模块自动配置：Servlet Web 应用自动注册 {@link GlobalExceptionHandler}。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(MoliResult.class)
@Import({GlobalExceptionHandler.class, com.moli.common.utils.SpringUtil.class, com.moli.common.core.IdGenerator.class})
public class MoliCommonAutoConfiguration {
}
