package com.moli.user.center.server.controller;


import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.SysRole;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.entity.SysUserRole;
import com.moli.user.center.common.domain.vo.UserRoleVo;
import com.moli.user.center.common.domain.vo.UserVo;
import com.moli.common.page.PageRes;
import com.moli.common.utils.MoliDateUtils;
import com.moli.user.center.server.mapper.RoleMapper;
import com.moli.user.center.server.mapper.UserMapper;
import com.moli.user.center.server.mapper.UserRoleMapper;
import com.moli.user.center.server.service.UserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 用户列表
     *
     * @param userVo
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "用户列表", notes = "用户列表")
    public MoliResult<PageRes<SysUser>> list(UserVo userVo) {
        PageRes<SysUser> result = new PageRes<>();
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper();
        if (userVo.getDeptId() != null) {
            lambdaQueryWrapper.eq(SysUser::getDeptId, userVo.getDeptId());
        }
        if (StringUtils.isNotBlank(userVo.getUserName())) {
            lambdaQueryWrapper.eq(SysUser::getUserName, userVo.getUserName());
        }
        if (StringUtils.isNotBlank(userVo.getTelephone())) {
            lambdaQueryWrapper.eq(SysUser::getTelephone, userVo.getTelephone());
        }
        if (userVo.getStatus() != null) {
            lambdaQueryWrapper.eq(SysUser::getStatus, userVo.getStatus());
        }
        if (userVo.getBeginTime() != null) {
            lambdaQueryWrapper.between(SysUser::getCreateTime, MoliDateUtils.startTimeToDateStart(userVo.getBeginTime()), userVo.getEndTime() + " 23:59:59");
        }
        lambdaQueryWrapper.eq(SysUser::getIsDelete, CommonConstant.UN_DELETE);
        Page page = new Page();
        page.setPages(userVo.getPageNum());
        page.setSize(userVo.getPageSize());
        userMapper.selectPage(page, lambdaQueryWrapper);
        Long total = page.getTotal();
        result.setTotal(total.intValue());
        result.setList(page.getRecords());
        result.setPageNum(userVo.getPageNum());
        result.setPageSize(userVo.getPageSize());
        return MoliResult.success(result);
    }

    /**
     * 添加用户
     *
     * @return 添加用户
     */
    @PostMapping
    public MoliResult<Boolean> insert(@RequestBody UserVo userVo) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userVo, user);
        userMapper.insert(user);
        return MoliResult.success(Boolean.TRUE);
    }

    /**
     * 更新用户
     *
     * @return
     */
    @PutMapping
    public MoliResult<Boolean> update(@RequestBody SysUser user) {
        userMapper.updateById(user);
        return MoliResult.success(Boolean.TRUE);
    }

    /**
     * 查询单个用户
     */
    @GetMapping(value = "/{id}")
    public MoliResult<SysUser> getInfo(@PathVariable("id") Long id) {

        return MoliResult.success(userMapper.selectById(id));
    }

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping(value = "/getInfoByUserName/{userName}")
    public MoliResult<SysUser> getInfoByUserName(@PathVariable("userName") String userName) {
        return MoliResult.success(userMapper.selectOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName, userName).eq(SysUser::getIsDelete, CommonConstant.UN_DELETE)));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userIds}")
    public MoliResult delete(@PathVariable Long[] userIds) {
        for (Long id : userIds) {
            SysUser user = new SysUser();
            user.setId(id);
            user.setIsDelete(CommonConstant.IS_DELETE);
            userMapper.updateById(user);
        }

        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping("/changeStatus")
    public MoliResult changeStatus(@RequestBody SysUser user) {
        userMapper.updateById(user);
        return MoliResult.success(Boolean.TRUE);
    }

    /**
     * 查询单个用户下的角色信息
     */
    @GetMapping(value = "/getRoleByUserId/{userId}")
    public MoliResult<UserRoleVo> getRoleByUserId(@PathVariable Long userId) {
        UserRoleVo userRoleVo = new UserRoleVo();
        SysUser user = userMapper.selectById(userId);
        userRoleVo.setUser(user);
        List<SysUserRole> sysUserRoleList = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<Long> roleIdList = sysUserRoleList.stream().map(e -> e.getRoleId()).collect(Collectors.toList());
        List<SysRole> sysRoleList = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIdList));
        userRoleVo.setSysRoleList(sysRoleList);
        return MoliResult.success(userRoleVo);
    }

    /**
     * 保存授权角色
     *
     * @return
     */
    @PutMapping("/inserUserRole")
    public MoliResult<Boolean> inserUserRole(@RequestBody UserRoleVo userRoleVo) {
        List<SysUserRole> sysUserRoleList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userRoleVo.getSysRoleList())) {
            for (SysRole sysRole : userRoleVo.getSysRoleList()) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(userRoleVo.getUserId());
                sysUserRole.setRoleId(sysRole.getId());
                sysUserRoleList.add(sysUserRole);
            }
            userRoleService.saveBatch(sysUserRoleList);
        }
        return MoliResult.success(Boolean.TRUE);
    }

}
