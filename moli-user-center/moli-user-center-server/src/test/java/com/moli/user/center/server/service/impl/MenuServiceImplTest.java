package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.constant.SystemConstant;
import com.moli.user.center.common.domain.entity.SysMenu;
import com.moli.user.center.common.domain.entity.SysRole;
import com.moli.user.center.common.domain.entity.SysRoleMenu;
import com.moli.user.center.common.domain.entity.SysSystem;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.entity.SysUserRole;
import com.moli.user.center.common.domain.vo.MenuVo;
import com.moli.user.center.server.config.util.ShiroUtils;
import com.moli.user.center.server.mapper.MenuMapper;
import com.moli.user.center.server.mapper.RoleMapper;
import com.moli.user.center.server.mapper.RoleMenuMapper;
import com.moli.user.center.server.mapper.SysSystemMapper;
import com.moli.user.center.server.mapper.SysUserMapper;
import com.moli.user.center.server.mapper.SysUserRoleMapper;
import com.moli.user.center.server.service.ConfigService;
import com.moli.user.center.server.sysparam.ConfigKey;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MenuServiceImplTest {

    @InjectMocks
    private MenuServiceImpl menuService;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private SysSystemMapper sysSystemMapper;

    @Mock
    private ConfigService configService;

    /** 门户开关改为运行期从 ConfigService 读取（原先是 @Value 注入的 ssoEnabled 字段） */
    private void stubPortalSwitch(boolean enabled) {
        when(configService.getBoolean(ConfigKey.SSO_ENABLED)).thenReturn(enabled);
    }

    @Test
    public void resolveRouters_portalEnabledWithoutCurrentSystem_returnsEmpty() {
        stubPortalSwitch(true);
        SysUser user = user("operator", 2L);
        when(sysUserMapper.selectById(2L)).thenReturn(user);
        when(sysSystemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1);

        try (MockedStatic<ShiroUtils> shiro = mockStatic(ShiroUtils.class)) {
            shiro.when(ShiroUtils::getCurrentSystemId).thenReturn(null);
            List<MenuVo> result = menuService.resolveRoutersForCurrentSystem(2L);
            Assert.assertTrue(result.isEmpty());
        }
    }

    @Test
    public void resolveRouters_portalDisabled_returnsUnfilteredRoleTree() {
        stubPortalSwitch(false);
        SysUser user = user("operator", 2L);
        when(sysUserMapper.selectById(2L)).thenReturn(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(10L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(Collections.singletonList(userRole));

        SysRole role = new SysRole();
        role.setId(10L);
        role.setStatus(CommonConstant.YES);
        when(roleMapper.selectList(any())).thenReturn(Collections.singletonList(role));

        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setMenuId(401L);
        when(roleMenuMapper.selectList(any())).thenReturn(Collections.singletonList(roleMenu));

        SysMenu parent = menu(400L, 0L, "M", null);
        SysMenu child = menu(401L, 400L, "C", 1L);
        when(menuMapper.selectById(401L)).thenReturn(child);
        when(menuMapper.selectById(400L)).thenReturn(parent);
        when(menuMapper.selectList(any())).thenReturn(Arrays.asList(parent, child));

        List<MenuVo> result = menuService.resolveRoutersForCurrentSystem(2L);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(400L, result.get(0).getId().longValue());
        Assert.assertEquals(1, result.get(0).getChildren().size());
    }

    @Test
    public void resolveRouters_filtersByCurrentSystemAndKeepsAncestors() {
        stubPortalSwitch(true);
        SysUser user = user("operator", 2L);
        when(sysUserMapper.selectById(2L)).thenReturn(user);
        when(sysSystemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2);

        SysSystem current = new SysSystem();
        current.setId(1L);
        current.setSsoMode(SystemConstant.SSO_MODE_INTERNAL);
        when(sysSystemMapper.selectById(1L)).thenReturn(current);

        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(10L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(Collections.singletonList(userRole));

        SysRole role = new SysRole();
        role.setId(10L);
        role.setStatus(CommonConstant.YES);
        when(roleMapper.selectList(any())).thenReturn(Collections.singletonList(role));

        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setMenuId(401L);
        when(roleMenuMapper.selectList(any())).thenReturn(Collections.singletonList(roleMenu));

        SysMenu parent = menu(400L, 0L, "M", 1L);
        SysMenu child = menu(401L, 400L, "C", 1L);
        SysMenu otherSystem = menu(600L, 0L, "M", 6L);
        when(menuMapper.selectById(401L)).thenReturn(child);
        when(menuMapper.selectById(400L)).thenReturn(parent);
        when(menuMapper.selectList(any())).thenReturn(Arrays.asList(parent, child, otherSystem));

        try (MockedStatic<ShiroUtils> shiro = mockStatic(ShiroUtils.class)) {
            shiro.when(ShiroUtils::getCurrentSystemId).thenReturn(1L);
            List<MenuVo> result = menuService.resolveRoutersForCurrentSystem(2L);
            Assert.assertEquals(1, result.size());
            Assert.assertEquals(400L, result.get(0).getId().longValue());
        }
    }

    @Test
    public void resolveRouters_externalCurrentSystem_returnsEmpty() {
        stubPortalSwitch(true);
        SysUser user = user("operator", 2L);
        when(sysUserMapper.selectById(2L)).thenReturn(user);
        when(sysSystemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2);

        SysSystem external = new SysSystem();
        external.setId(39L);
        external.setSsoMode(SystemConstant.SSO_MODE_EXTERNAL);
        when(sysSystemMapper.selectById(39L)).thenReturn(external);

        try (MockedStatic<ShiroUtils> shiro = mockStatic(ShiroUtils.class)) {
            shiro.when(ShiroUtils::getCurrentSystemId).thenReturn(39L);
            List<MenuVo> result = menuService.resolveRoutersForCurrentSystem(2L);
            Assert.assertTrue(result.isEmpty());
        }
    }

    private static SysUser user(String userName, Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserName(userName);
        return user;
    }

    private static SysMenu menu(Long id, Long parentId, String menuType, Long systemId) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuType(menuType);
        menu.setSystemId(systemId);
        menu.setStatus(CommonConstant.YES);
        menu.setMenuName("menu-" + id);
        menu.setPath("path-" + id);
        menu.setOrderNum(1);
        return menu;
    }
}
