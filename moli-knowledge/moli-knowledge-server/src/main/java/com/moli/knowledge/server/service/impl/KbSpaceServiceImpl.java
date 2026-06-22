package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.enums.SpaceVisibility;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbSpaceService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class KbSpaceServiceImpl implements KbSpaceService {

    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public Page<KbSpace> page(KbSpace query, int pageNum, int pageSize) {
        // ACL：仅返回当前用户可读的空间
        List<Long> accessible = kbAclService.accessibleSpaceIds();
        if (accessible.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }
        LambdaQueryWrapper<KbSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbSpace::getIsDelete, CommonConstant.UN_DELETE);
        wrapper.in(KbSpace::getId, accessible);
        if (query != null) {
            if (StringUtils.isNotBlank(query.getSpaceName())) {
                wrapper.like(KbSpace::getSpaceName, query.getSpaceName());
            }
            if (query.getStatus() != null) {
                wrapper.eq(KbSpace::getStatus, query.getStatus());
            }
        }
        wrapper.orderByAsc(KbSpace::getSort).orderByDesc(KbSpace::getCreateTime);
        return kbSpaceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public KbSpace getById(Long id) {
        KbSpace space = kbSpaceMapper.selectById(id);
        if (space == null || !CommonConstant.UN_DELETE.equals(space.getIsDelete())) {
            throw new BaseException("知识空间不存在");
        }
        kbAclService.assertCanRead(id);
        return space;
    }

    @Override
    public Long create(KbSpace space) {
        if (StringUtils.isBlank(space.getSpaceCode())) {
            throw new BaseException("空间编码不能为空");
        }
        Integer exists = kbSpaceMapper.selectCount(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getSpaceCode, space.getSpaceCode())
                .eq(KbSpace::getIsDelete, CommonConstant.UN_DELETE));
        if (exists != null && exists > 0) {
            throw new BaseException("空间编码已存在");
        }
        space.setId(IdGenerator.getId());
        if (space.getVisibility() == null) {
            space.setVisibility(SpaceVisibility.INTERNAL.getCode());
        }
        if (space.getStatus() == null) {
            space.setStatus(CommonConstant.YES);
        }
        if (space.getSort() == null) {
            space.setSort(0);
        }
        if (space.getOwnerId() == null) {
            space.setOwnerId(ShiroUtils.getUserId());
        }
        kbSpaceMapper.insert(space);
        return space.getId();
    }

    @Override
    public void update(KbSpace space) {
        getById(space.getId());
        kbAclService.assertCanAdmin(space.getId());
        kbSpaceMapper.updateById(space);
    }

    @Override
    public void delete(Long id) {
        KbSpace space = getById(id);
        kbAclService.assertCanAdmin(id);
        space.setIsDelete(CommonConstant.IS_DELETE);
        kbSpaceMapper.updateById(space);
    }
}
