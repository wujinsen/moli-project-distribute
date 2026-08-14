package com.moli.ai.server.bi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.ai.server.bi.config.BiChatProperties;
import com.moli.ai.server.bi.dto.BiChatAskRequest;
import com.moli.ai.server.bi.dto.BiChatTraceVo;
import com.moli.ai.server.bi.dto.BiTraceStep;
import com.moli.ai.server.bi.security.BiSqlSecurityValidator;
import com.moli.ai.server.bi.support.BiAgentClient;
import com.moli.ai.server.bi.support.BiChatReadonlyQueryExecutor;
import com.moli.user.center.common.domain.entity.SysUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * B2 回归：SSE worker 线程无 Shiro 上下文时，仍持久化请求线程解析的 userId（INV-11/12）。
 */
public class BiChatServiceImplStreamUserIdTest {

    private DefaultSecurityManager securityManager;

    @Before
    public void setUpSecurityManager() {
        securityManager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);
    }

    @After
    public void tearDownSecurityManager() {
        SecurityUtils.setSecurityManager(null);
    }

    @Test
    public void askStreamPersistsUserIdResolvedOnRequestThread() throws Exception {
        CountDownLatch saved = new CountDownLatch(1);
        AtomicReference<Long> capturedUserId = new AtomicReference<>(-1L);

        AiChatTraceService traceService = new AiChatTraceService() {
            @Override
            public void saveTrace(String traceId, String sessionId, Long userId, String question,
                                  String finalSql, String status, String rejectCode, String rejectReason,
                                  Integer rowCount, Long latencyMs, Integer retry, List<BiTraceStep> steps) {
                capturedUserId.set(userId);
                saved.countDown();
            }

            @Override
            public BiChatTraceVo getTrace(String traceId, Long currentUserId, boolean traceAllPermitted) {
                return null;
            }
        };

        BiChatProperties props = new BiChatProperties();
        props.setAllowTables(Arrays.asList("seckill_order", "seckill_activity"));

        BiChatServiceImpl service = new BiChatServiceImpl(
                props,
                new BiAgentClient(new com.moli.ai.server.bi.config.BiAgentProperties(), new ObjectMapper()),
                new BiSqlSecurityValidator(props),
                new BiChatReadonlyQueryExecutor(null, props),
                traceService,
                new ObjectMapper()
        );

        SysUser user = new SysUser();
        user.setId(42L);
        Subject subject = new Subject.Builder(securityManager)
                .principals(new SimplePrincipalCollection(user, "testRealm"))
                .buildSubject();

        BiChatAskRequest request = new BiChatAskRequest();
        request.setQuestion("");
        request.setStream(true);

        subject.execute(() -> {
            service.askStream(request);
            return null;
        });

        Assert.assertTrue("trace not saved", saved.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(Long.valueOf(42L), capturedUserId.get());
    }
}
