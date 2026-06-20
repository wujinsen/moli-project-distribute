package com.moli.user.center.server.api;

import com.moli.user.center.common.domain.vo.SsoValidateReq;
import com.moli.user.center.common.domain.vo.SsoValidateVo;
import com.moli.user.center.server.controller.SsoController;
import com.moli.user.center.server.service.SsoService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoControllerApiTest extends AbstractApiTest {

    @InjectMocks
    private SsoController controller;

    @Mock
    private SsoService ssoService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(controller, "sharedSecret", "");
    }

    @Test
    public void POST_sso_validate_success() {
        SsoValidateReq req = new SsoValidateReq();
        req.setTicket("t1");
        req.setSystemCode("crm-demo");
        when(ssoService.validateTicket("t1", "crm-demo")).thenReturn(new SsoValidateVo());
        ControllerTestSupport.assertSuccess(controller.validate(req, null));
    }

    @Test
    public void POST_sso_validate_invalidSecret() {
        ReflectionTestUtils.setField(controller, "sharedSecret", "secret");
        SsoValidateReq req = new SsoValidateReq();
        req.setTicket("t1");
        req.setSystemCode("crm-demo");
        Assert.assertEquals(10009, controller.validate(req, "wrong").getCode());
    }
}
