package com.moli.user.center.starter.autoconfigure;

import com.moli.user.center.api.UserCenterServer;
import com.moli.user.center.starter.shiro.AuthenticationFilter;
import com.moli.user.center.starter.shiro.ShiroRealm;
import com.moli.user.center.starter.shiro.ShiroSessionIdGenerator;
import com.moli.user.center.starter.shiro.ShiroSessionManager;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SecurityManager.class, ShiroFilterFactoryBean.class})
@ConditionalOnProperty(prefix = "moli.user-center.shiro", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(UserCenterShiroProperties.class)
public class UserCenterShiroAutoConfiguration {

    private static final String CACHE_KEY = "shiro:cache:";
    private static final String SESSION_KEY = "shiro:session:";

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.timeout:3000}")
    private int redisTimeout;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @DubboReference(version = "1.0.0", group = "moli", protocol = "dubbo", check = false)
    private UserCenterServer userCenterServer;

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactory(SecurityManager securityManager,
                                                     AuthenticationFilter authenticationFilter,
                                                     UserCenterShiroProperties properties) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v2/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/static/**", "anon");
        if (properties.getAnonPaths() != null) {
            for (String path : properties.getAnonPaths()) {
                if (path != null && !path.trim().isEmpty()) {
                    filterChainDefinitionMap.put(path.trim(), "anon");
                }
            }
        }
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.getFilters().put("authc", authenticationFilter);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationFilter authenticationFilter() {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.setUserCenterServer(userCenterServer);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityManager securityManager(ShiroRealm shiroRealm, SessionManager sessionManager,
                                           RedisCacheManager cacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setSessionManager(sessionManager);
        securityManager.setCacheManager(cacheManager);
        securityManager.setRealm(shiroRealm);
        return securityManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public ShiroRealm shiroRealm() {
        ShiroRealm shiroRealm = new ShiroRealm();
        shiroRealm.setUserCenterServer(userCenterServer);
        return shiroRealm;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisManager shiroRedisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(redisHost);
        redisManager.setPort(redisPort);
        redisManager.setTimeout(redisTimeout);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisManager.setPassword(redisPassword);
        }
        redisManager.setDatabase(redisDatabase);
        return redisManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheManager cacheManager(RedisManager shiroRedisManager) {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(shiroRedisManager);
        redisCacheManager.setKeyPrefix(CACHE_KEY);
        redisCacheManager.setPrincipalIdFieldName("userName");
        return redisCacheManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public ShiroSessionIdGenerator sessionIdGenerator() {
        return new ShiroSessionIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisSessionDAO redisSessionDAO(RedisManager shiroRedisManager,
                                           ShiroSessionIdGenerator sessionIdGenerator,
                                           UserCenterShiroProperties properties) {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(shiroRedisManager);
        redisSessionDAO.setSessionIdGenerator(sessionIdGenerator);
        redisSessionDAO.setKeyPrefix(SESSION_KEY);
        redisSessionDAO.setExpire(properties.getSessionExpireSeconds());
        return redisSessionDAO;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager(RedisSessionDAO redisSessionDAO, UserCenterShiroProperties properties) {
        ShiroSessionManager shiroSessionManager = new ShiroSessionManager();
        shiroSessionManager.setSessionDAO(redisSessionDAO);
        shiroSessionManager.setGlobalSessionTimeout(properties.getSessionExpireSeconds() * 1000L);
        return shiroSessionManager;
    }
}
