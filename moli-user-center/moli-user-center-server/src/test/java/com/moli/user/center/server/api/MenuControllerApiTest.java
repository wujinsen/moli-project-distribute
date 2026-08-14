package com.moli.user.center.server.api;

import com.moli.user.center.common.domain.entity.SysMenu;
import com.moli.user.center.server.controller.MenuController;
import com.moli.user.center.server.mapper.MenuMapper;
import com.moli.user.center.server.service.MenuService;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ShiroMockSupport;
import org.apache.shiro.SecurityUtils;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MenuControllerApiTest extends AbstractApiTest {

    @InjectMocks
    private MenuController controller;

    @Mock
    private MenuService menuService;

    @Mock
    private MenuMapper menuMapper;

    @Test
    public void GET_menu_getRouters() {
        when(menuService.resolveRoutersForCurrentSystem(2L)).thenReturn(Collections.emptyList());
        try (MockedStatic<SecurityUtils> shiro = ShiroMockSupport.mockUser("operator", 2L)) {
            ControllerTestSupport.assertSuccess(controller.getRouters());
        }
    }

    @Test
    public void GET_menu_list() {
        when(menuService.selectMenuList(any())).thenReturn(Collections.emptyList());
        ControllerTestSupport.assertSuccess(controller.list("系统", 1));
    }

    @Test
    public void POST_menu_insert() {
        ControllerTestSupport.assertSuccess(controller.insert(new SysMenu()));
    }

    @Test
    public void PUT_menu_update() {
        ControllerTestSupport.assertSuccess(controller.update(new SysMenu()));
    }

    @Test
    public void GET_menu_id() {
        ControllerTestSupport.stubSelectById(menuMapper, new SysMenu());
        ControllerTestSupport.assertSuccess(controller.getInfo(1L));
    }

    @Test
    public void DELETE_menu_id() {
        ControllerTestSupport.assertSuccess(controller.remove(1L));
    }

    @Test
    public void GET_menu_selectMenuTreeByRoleId() {
        when(menuService.selectMenuTreeByRoleId(1L)).thenReturn(Collections.emptyList());
        ControllerTestSupport.assertSuccess(controller.selectMenuTreeByRoleId(1L));
    }

    @Test
    public void GET_menu_getMenuTreeAll() {
        when(menuService.getMenuTreeAll()).thenReturn(Collections.emptyList());
        ControllerTestSupport.assertSuccess(controller.getMenuTreeAll());
    }
}
