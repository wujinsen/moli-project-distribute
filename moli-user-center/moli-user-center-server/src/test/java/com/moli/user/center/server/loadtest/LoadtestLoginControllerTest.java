package com.moli.user.center.server.loadtest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.enums.ResponseCodeEnums;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.server.mapper.SysUserMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadtestLoginControllerTest {

    @InjectMocks
    private LoadtestLoginController controller;

    @Mock
    private SysUserMapper sysUserMapper;

    @Test
    public void POST_loadtestLogin_userNotFound() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        SysUser req = new SysUser();
        req.setUserName("nobody");
        Assert.assertEquals((int) ResponseCodeEnums.ERROR.getCode(), controller.login(req).getCode());
    }
}
