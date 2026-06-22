package com.moli.user.center.server.loadtest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.MoliResult;
import com.moli.common.enums.ResponseCodeEnums;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.vo.LoginVo;
import com.moli.user.center.server.config.util.ShiroUtils;
import com.moli.user.center.server.mapper.SysUserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 压测专用登录入口：仅 Shiro 认证 + Session，不写登录日志、不查菜单/系统门户。
 * <p>
 * 仅在 {@code loadtest} profile 注册；k6 等脚本应调用 {@code POST /loadtest/login}，
 * 产品登录请继续使用 {@code POST /login}。
 */
@RestController
@RequestMapping("/loadtest")
@Api(tags = "压测专用（loadtest profile）")
@Profile("loadtest")
@Slf4j
public class LoadtestLoginController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @PostMapping("/login")
    @ApiOperation(value = "压测登录", notes = "非产品接口，仅 loadtest profile 可用")
    public MoliResult<LoginVo> login(@RequestBody SysUser request) {
        String userName = request.getUserName();
        MoliResult<LoginVo> result = new MoliResult<>();
        LoginVo loginVo = new LoginVo();

        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().lambda()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getIsDelete, CommonConstant.UN_DELETE));
        if (user == null) {
            result.setMsg("用户不存在或者密码错误");
            result.setCode(ResponseCodeEnums.ERROR.getCode());
            return result;
        }

        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(userName, request.getPassword()));
        } catch (IncorrectCredentialsException e) {
            result.setMsg("用户不存在或者密码错误");
            result.setCode(ResponseCodeEnums.ERROR.getCode());
            user.setPassword("");
            user.setSalt("");
            loginVo.setUser(user);
            result.setData(loginVo);
            return result;
        } catch (LockedAccountException e) {
            result.setMsg("登录失败，该用户已被冻结");
            result.setCode(ResponseCodeEnums.ERROR.getCode());
            return result;
        } catch (AuthenticationException e) {
            result.setMsg("用户认证失败");
            result.setCode(ResponseCodeEnums.ERROR.getCode());
            log.error("loadtest login AuthenticationException: {}", e.getMessage());
            return result;
        }

        loginVo.setToken(ShiroUtils.getSession().getId().toString());
        ShiroUtils.bindUserSession(user.getUserName());
        user.setPassword("");
        user.setSalt("");
        loginVo.setUser(user);
        result.setMsg("登录成功");
        result.setCode(ResponseCodeEnums.SUCCESS_CODE.getCode());
        result.setData(loginVo);
        return result;
    }
}
