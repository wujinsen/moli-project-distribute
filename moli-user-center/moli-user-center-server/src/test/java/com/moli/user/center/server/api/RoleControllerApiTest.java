package com.moli.user.center.server.api;

import com.moli.user.center.common.domain.entity.SysRole;
import com.moli.user.center.common.domain.vo.RoleVo;
import com.moli.user.center.common.domain.vo.SysRoleVo;
import com.moli.user.center.server.controller.RoleController;
import com.moli.user.center.server.mapper.RoleActionMapper;
import com.moli.user.center.server.mapper.RoleMapper;
import com.moli.user.center.server.mapper.RoleMenuMapper;
import com.moli.user.center.server.mapper.SysUserMapper;
import com.moli.user.center.server.mapper.SysUserRoleMapper;
import com.moli.user.center.server.service.RoleAuthService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import com.moli.user.center.server.testsupport.ShiroMockSupport;
import org.apache.shiro.SecurityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoleControllerApiTest extends AbstractApiTest {

    @InjectMocks
    private RoleController controller;

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleMenuMapper roleMenuMapper;
    @Mock
    private RoleActionMapper roleActionMapper;
    @Mock
    private RoleAuthService roleAuthService;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysUserMapper sysUserMapper;

    @Test
    public void GET_role_list() {
        ControllerTestSupport.stubEmptyPage(roleMapper);
        RoleVo vo = new RoleVo();
        vo.setPageNum(1);
        vo.setPageSize(10);
        ControllerTestSupport.assertSuccess(controller.list(vo));
    }

    @Test
    public void POST_role_insert() {
        when(roleMapper.insert(any(SysRole.class))).thenAnswer(inv -> {
            ((SysRole) inv.getArgument(0)).setId(1L);
            return 1;
        });
        ControllerTestSupport.assertSuccess(controller.insert(new RoleVo()));
    }

    @Test
    public void PUT_role_update() {
        try (org.mockito.MockedStatic<SecurityUtils> shiro = ShiroMockSupport.mockSuperadmin()) {
            ControllerTestSupport.stubUpdate(roleMapper);
            ControllerTestSupport.stubSelectListEmpty(sysUserRoleMapper);
            SysRoleVo vo = new SysRoleVo();
            vo.setId(1L);
            ControllerTestSupport.assertSuccess(controller.update(vo));
        }
    }

    @Test
    public void GET_role_id() {
        ControllerTestSupport.stubSelectById(roleMapper, new SysRole());
        ControllerTestSupport.assertSuccess(controller.getInfo(1L));
    }

    @Test
    public void DELETE_role_ids() {
        ControllerTestSupport.assertSuccess(controller.delete(new Long[]{1L}));
    }

    @Test
    public void PUT_role_changeStatus() {
        ControllerTestSupport.stubUpdate(roleMapper);
        ControllerTestSupport.assertSuccess(controller.changeStatus(new SysRole()));
    }

    @Test
    public void GET_role_getRoleAll() {
        ControllerTestSupport.stubSelectListEmpty(roleMapper);
        ControllerTestSupport.assertSuccess(controller.getRoleAll());
    }
}
