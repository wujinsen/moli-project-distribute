package com.moli.user.center.server.service;

import com.moli.common.constant.CommonConstant;
import com.moli.user.center.common.domain.entity.SysAction;
import com.moli.user.center.common.domain.entity.SysMenu;
import com.moli.user.center.common.domain.entity.SysRole;
import com.moli.user.center.common.domain.entity.SysRoleAction;
import com.moli.user.center.common.domain.entity.SysRoleMenu;
import com.moli.user.center.common.domain.entity.SysUserRole;
import com.moli.user.center.server.mapper.ActionMapper;
import com.moli.user.center.server.mapper.MenuMapper;
import com.moli.user.center.server.mapper.RoleActionMapper;
import com.moli.user.center.server.mapper.RoleMapper;
import com.moli.user.center.server.mapper.RoleMenuMapper;
import com.moli.user.center.server.mapper.SysUserRoleMapper;
import com.moli.user.center.server.service.impl.PermissionServiceImpl;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.MybatisPlusTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceImplTest extends AbstractApiTest {

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private RoleMenuMapper roleMenuMapper;
    @Mock
    private RoleActionMapper roleActionMapper;
    @Mock
    private ActionMapper actionMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private MenuMapper menuMapper;

    @Before
    public void initMetadata() {
        MybatisPlusTestSupport.initAll();
    }

    @Test
    public void superAdminGetsWildcard() {
        Set<String> perms = permissionService.getPermissionsByUserId(1L, CommonConstant.SUPER_ADMIN);
        assertTrue(perms.contains("*:*:*"));
    }

    @Test
    public void unionsPageAndActionPerms() {
        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(2L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(Collections.singletonList(userRole));

        SysRole role = new SysRole();
        role.setId(2L);
        role.setStatus(CommonConstant.YES);
        when(roleMapper.selectList(any())).thenReturn(Collections.singletonList(role));

        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setMenuId(10L);
        when(roleMenuMapper.selectList(any())).thenReturn(Collections.singletonList(roleMenu));

        SysMenu menu = new SysMenu();
        menu.setId(10L);
        menu.setMenuType("C");
        menu.setPerms("system:user:list");
        menu.setStatus(1);
        when(menuMapper.selectList(any())).thenReturn(Collections.singletonList(menu));

        SysRoleAction roleAction = new SysRoleAction();
        roleAction.setPermCode("system:user:add");
        when(roleActionMapper.selectList(any())).thenReturn(Collections.singletonList(roleAction));

        SysAction action = new SysAction();
        action.setPermCode("system:user:add");
        action.setStatus(CommonConstant.YES);
        when(actionMapper.selectList(any())).thenReturn(Collections.singletonList(action));

        Set<String> perms = permissionService.getPermissionsByUserId(99L, "operator");
        assertEquals(2, perms.size());
        assertTrue(perms.containsAll(Arrays.asList("system:user:list", "system:user:add")));
    }
}
