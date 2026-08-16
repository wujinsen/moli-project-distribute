package com.moli.user.center.server.api;

import com.moli.user.center.common.domain.vo.ConfigUpdateRequest;
import com.moli.user.center.server.controller.ConfigController;
import com.moli.user.center.server.service.ConfigService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerApiTest extends AbstractApiTest {

    @InjectMocks
    private ConfigController controller;

    @Mock
    private ConfigService configService;

    @Test
    public void GET_config_list() {
        when(configService.listItems(null)).thenReturn(Collections.emptyList());
        ControllerTestSupport.assertSuccess(controller.list(null));
    }

    @Test
    public void GET_config_list_byGroup() {
        when(configService.listItems("OPS")).thenReturn(Collections.emptyList());
        ControllerTestSupport.assertSuccess(controller.list("OPS"));
    }

    @Test
    public void PUT_config_update() {
        ConfigUpdateRequest request = new ConfigUpdateRequest();
        request.setConfigKey("captcha.enabled");
        request.setConfigValue("true");

        ControllerTestSupport.assertSuccess(controller.update(request));
        verify(configService).setOverride("captcha.enabled", "true");
    }

    @Test
    public void DELETE_config_reset() {
        ControllerTestSupport.assertSuccess(controller.reset("captcha.enabled"));
        verify(configService).resetToDefault("captcha.enabled");
    }
}
