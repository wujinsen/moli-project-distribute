package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbSpaceMember;
import com.moli.knowledge.server.mapper.KbSpaceMemberMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbSpaceMemberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class KbSpaceMemberServiceImpl implements KbSpaceMemberService {

    private static final List<String> ROLES = java.util.Arrays.asList("viewer", "editor", "admin");

    @Resource
    private KbSpaceMemberMapper kbSpaceMemberMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public List<KbSpaceMember> list(Long spaceId) {
        kbAclService.assertCanAdmin(spaceId);
        return kbSpaceMemberMapper.selectList(new LambdaQueryWrapper<KbSpaceMember>()
                .eq(KbSpaceMember::getSpaceId, spaceId)
                .eq(KbSpaceMember::getIsDelete, CommonConstant.UN_DELETE));
    }

    @Override
    public Long add(KbSpaceMember member) {
        validate(member);
        kbAclService.assertCanAdmin(member.getSpaceId());
        Integer exists = kbSpaceMemberMapper.selectCount(new LambdaQueryWrapper<KbSpaceMember>()
                .eq(KbSpaceMember::getSpaceId, member.getSpaceId())
                .eq(KbSpaceMember::getMemberType, member.getMemberType())
                .eq(KbSpaceMember::getMemberId, member.getMemberId())
                .eq(KbSpaceMember::getIsDelete, CommonConstant.UN_DELETE));
        if (exists != null && exists > 0) {
            throw new BaseException("该成员已存在");
        }
        member.setId(IdGenerator.getId());
        member.setIsDelete(CommonConstant.UN_DELETE);
        kbSpaceMemberMapper.insert(member);
        return member.getId();
    }

    @Override
    public void update(KbSpaceMember member) {
        if (member.getId() == null) {
            throw new BaseException("成员ID不能为空");
        }
        KbSpaceMember existing = kbSpaceMemberMapper.selectById(member.getId());
        if (existing == null || !CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {
            throw new BaseException("成员不存在");
        }
        kbAclService.assertCanAdmin(existing.getSpaceId());
        if (StringUtils.isNotBlank(member.getRole()) && !ROLES.contains(member.getRole())) {
            throw new BaseException("非法角色：" + member.getRole());
        }
        existing.setRole(member.getRole());
        kbSpaceMemberMapper.updateById(existing);
    }

    @Override
    public void remove(Long id) {
        KbSpaceMember existing = kbSpaceMemberMapper.selectById(id);
        if (existing == null || !CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {
            return;
        }
        kbAclService.assertCanAdmin(existing.getSpaceId());
        existing.setIsDelete(CommonConstant.IS_DELETE);
        kbSpaceMemberMapper.updateById(existing);
    }

    private void validate(KbSpaceMember member) {
        if (member.getSpaceId() == null || member.getMemberId() == null) {
            throw new BaseException("空间ID与成员ID不能为空");
        }
        if (member.getMemberType() == null) {
            member.setMemberType(0);
        }
        if (StringUtils.isBlank(member.getRole())) {
            member.setRole("viewer");
        } else if (!ROLES.contains(member.getRole())) {
            throw new BaseException("非法角色：" + member.getRole());
        }
    }
}
