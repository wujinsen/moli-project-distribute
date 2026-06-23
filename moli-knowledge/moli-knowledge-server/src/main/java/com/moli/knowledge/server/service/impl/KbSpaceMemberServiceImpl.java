package com.moli.knowledge.server.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.moli.common.constant.CommonConstant;

import com.moli.common.core.IdGenerator;

import com.moli.common.exception.BaseException;

import com.moli.knowledge.server.dto.KbSpaceMemberBatchAddRequest;

import com.moli.knowledge.server.dto.KbSpaceMemberBatchRemoveRequest;

import com.moli.knowledge.server.dto.KbSpaceMemberBatchResult;

import com.moli.knowledge.server.entity.KbSpaceMember;

import com.moli.knowledge.server.mapper.KbSpaceMemberMapper;

import com.moli.knowledge.server.service.KbAclService;

import com.moli.knowledge.server.service.KbSpaceMemberService;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;



import javax.annotation.Resource;

import java.util.List;



@Service

public class KbSpaceMemberServiceImpl implements KbSpaceMemberService {



    // 成员角色仅 viewer(只读)/editor(可改内容)；管理能力由超管/管理员的系统动作权限承担，不在成员表授予
    private static final List<String> ROLES = java.util.Arrays.asList("viewer", "editor");



    @Resource

    private KbSpaceMemberMapper kbSpaceMemberMapper;

    @Resource

    private KbAclService kbAclService;



    @Override

    public List<KbSpaceMember> list(Long spaceId) {

        kbAclService.assertCanManageMembers(spaceId);

        return kbSpaceMemberMapper.selectList(new LambdaQueryWrapper<KbSpaceMember>()

                .eq(KbSpaceMember::getSpaceId, spaceId)

                .eq(KbSpaceMember::getIsDelete, CommonConstant.UN_DELETE));

    }



    @Override

    public Long add(KbSpaceMember member) {

        validate(member);

        kbAclService.assertCanManageMembers(member.getSpaceId());

        Long id = upsertMember(member);

        if (id == null) {

            throw new BaseException("该成员已存在");

        }

        return id;

    }



    @Override

    public KbSpaceMemberBatchResult batchAdd(KbSpaceMemberBatchAddRequest request) {

        if (request == null || request.getSpaceId() == null) {

            throw new BaseException("空间ID不能为空");

        }

        if (CollectionUtils.isEmpty(request.getMemberIds())) {

            throw new BaseException("成员ID列表不能为空");

        }

        kbAclService.assertCanManageMembers(request.getSpaceId());

        int memberType = request.getMemberType() != null ? request.getMemberType() : 0;

        String role = StringUtils.isBlank(request.getRole()) ? "viewer" : request.getRole();

        if (!ROLES.contains(role)) {

            throw new BaseException("非法角色：" + role);

        }



        KbSpaceMemberBatchResult result = new KbSpaceMemberBatchResult();

        for (Long memberId : request.getMemberIds()) {

            if (memberId == null) {

                result.setFailCount(result.getFailCount() + 1);

                continue;

            }

            try {

                KbSpaceMember member = new KbSpaceMember();

                member.setSpaceId(request.getSpaceId());

                member.setMemberType(memberType);

                member.setMemberId(memberId);

                member.setRole(role);

                validate(member);

                Long rowId = upsertMember(member);

                if (rowId == null) {

                    result.setSkipCount(result.getSkipCount() + 1);

                } else {

                    result.setSuccessCount(result.getSuccessCount() + 1);

                    result.getMemberRowIds().add(rowId);

                }

            } catch (Exception e) {

                result.setFailCount(result.getFailCount() + 1);

            }

        }

        return result;

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

        kbAclService.assertCanManageMembers(existing.getSpaceId());

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

        kbAclService.assertCanManageMembers(existing.getSpaceId());

        existing.setIsDelete(CommonConstant.IS_DELETE);

        kbSpaceMemberMapper.updateById(existing);

    }



    @Override

    public KbSpaceMemberBatchResult batchRemove(KbSpaceMemberBatchRemoveRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getIds())) {

            throw new BaseException("成员ID列表不能为空");

        }

        KbSpaceMemberBatchResult result = new KbSpaceMemberBatchResult();

        for (Long id : request.getIds()) {

            if (id == null) {

                result.setFailCount(result.getFailCount() + 1);

                continue;

            }

            try {

                KbSpaceMember existing = kbSpaceMemberMapper.selectById(id);

                if (existing == null || !CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {

                    result.setSkipCount(result.getSkipCount() + 1);

                    continue;

                }

                kbAclService.assertCanManageMembers(existing.getSpaceId());

                existing.setIsDelete(CommonConstant.IS_DELETE);

                kbSpaceMemberMapper.updateById(existing);

                result.setSuccessCount(result.getSuccessCount() + 1);

                result.getMemberRowIds().add(id);

            } catch (Exception e) {

                result.setFailCount(result.getFailCount() + 1);

            }

        }

        return result;

    }



    /** 新增或恢复成员；已是有效成员时返回 null。 */

    private Long upsertMember(KbSpaceMember member) {

        KbSpaceMember existing = kbSpaceMemberMapper.selectOne(new LambdaQueryWrapper<KbSpaceMember>()

                .eq(KbSpaceMember::getSpaceId, member.getSpaceId())

                .eq(KbSpaceMember::getMemberType, member.getMemberType())

                .eq(KbSpaceMember::getMemberId, member.getMemberId())

                .last("limit 1"));

        if (existing != null) {

            if (CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {

                return null;

            }

            existing.setIsDelete(CommonConstant.UN_DELETE);

            existing.setRole(member.getRole());

            kbSpaceMemberMapper.updateById(existing);

            return existing.getId();

        }

        member.setId(IdGenerator.getId());

        member.setIsDelete(CommonConstant.UN_DELETE);

        kbSpaceMemberMapper.insert(member);

        return member.getId();

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


