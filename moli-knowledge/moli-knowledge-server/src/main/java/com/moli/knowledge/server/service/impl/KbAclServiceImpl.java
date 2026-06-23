package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.constant.PermissionConstants;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.entity.KbSpaceMember;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.mapper.KbSpaceMemberMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KbAclServiceImpl implements KbAclService {

    /** 全局管理员权限串。 */
    private static final String ADMIN_PERM = "kb:admin";

    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbSpaceMemberMapper kbSpaceMemberMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;

    @Override
    public boolean isAdmin() {
        try {
            SysUser user = ShiroUtils.getUserInfo();
            if (user != null && CommonConstant.hasFullPermission(user.getUserName())) {
                return true;
            }
            return SecurityUtils.getSubject().isPermitted(ADMIN_PERM)
                    || SecurityUtils.getSubject().isPermitted(PermissionConstants.SUPER_ADMIN);
        } catch (Exception e) {                            // 未登录/无 Subject
            return false;
        }
    }

    @Override
    public boolean canRead(Long spaceId) {
        if (spaceId == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        KbSpace space = loadSpace(spaceId);
        if (space == null) {
            return false;
        }
        return readable(space, ShiroUtils.getUserId());
    }

    @Override
    public boolean canEdit(Long spaceId) {
        if (spaceId == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        KbSpace space = loadSpace(spaceId);
        if (space == null) {
            return false;
        }
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return false;
        }
        if (userId.equals(space.getOwnerId())) {
            return true;
        }
        String role = memberRole(spaceId, userId);
        return "editor".equals(role) || "admin".equals(role);
    }

    @Override
    public boolean canAdmin(Long spaceId) {
        if (spaceId == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        KbSpace space = loadSpace(spaceId);
        if (space == null) {
            return false;
        }
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return userId.equals(space.getOwnerId()) || "admin".equals(memberRole(spaceId, userId));
    }

    @Override
    public void assertCanRead(Long spaceId) {
        if (!canRead(spaceId)) {
            throw new BaseException("无权访问该知识空间");
        }
    }

    @Override
    public void assertCanEdit(Long spaceId) {
        if (!canEdit(spaceId)) {
            throw new BaseException("无权编辑该知识空间内容");
        }
    }

    @Override
    public void assertCanAdmin(Long spaceId) {
        if (!canAdmin(spaceId)) {
            throw new BaseException("无权管理该知识空间");
        }
    }

    @Override
    public void assertCanReadDocument(Long documentId) {
        assertCanRead(requireDocumentSpaceId(documentId));
    }

    @Override
    public void assertCanEditDocument(Long documentId) {
        assertCanEdit(requireDocumentSpaceId(documentId));
    }

    @Override
    public List<Long> resolveReadableSpaceIds(Long spaceId, List<Long> spaceIds) {
        if (spaceIds != null && !spaceIds.isEmpty()) {
            Set<Long> distinct = spaceIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            for (Long id : distinct) {
                assertCanRead(id);
            }
            return new ArrayList<>(distinct);
        }
        if (spaceId != null) {
            assertCanRead(spaceId);
            return Collections.singletonList(spaceId);
        }
        return accessibleSpaceIds();
    }

    @Override
    public List<Long> accessibleSpaceIds() {
        List<KbSpace> spaces = kbSpaceMapper.selectList(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getIsDelete, CommonConstant.UN_DELETE));
        List<Long> ids = new ArrayList<>();
        boolean admin = isAdmin();
        Long userId = ShiroUtils.getUserId();
        for (KbSpace s : spaces) {
            if (admin || readable(s, userId)) {
                ids.add(s.getId());
            }
        }
        return ids;
    }

    private Long requireDocumentSpaceId(Long documentId) {
        if (documentId == null) {
            throw new BaseException("文档ID不能为空");
        }
        KbDocument doc = kbDocumentMapper.selectById(documentId);
        if (doc == null || !CommonConstant.UN_DELETE.equals(doc.getIsDelete())) {
            throw new BaseException("文档不存在");
        }
        return doc.getSpaceId();
    }

    // ------------------------------------------------------------------

    private boolean readable(KbSpace space, Long userId) {
        if (userId == null) {
            return false;
        }
        // 未在 kb_space_member 分配的用户不可见；负责人默认可读
        if (userId.equals(space.getOwnerId())) {
            return true;
        }
        return memberRole(space.getId(), userId) != null;
    }

    private KbSpace loadSpace(Long spaceId) {
        KbSpace space = kbSpaceMapper.selectById(spaceId);
        if (space == null || !CommonConstant.UN_DELETE.equals(space.getIsDelete())) {
            return null;
        }
        return space;
    }

    /** 用户型成员(member_type=0)的角色；非成员返回 null。 */
    private String memberRole(Long spaceId, Long userId) {
        KbSpaceMember m = kbSpaceMemberMapper.selectOne(new LambdaQueryWrapper<KbSpaceMember>()
                .eq(KbSpaceMember::getSpaceId, spaceId)
                .eq(KbSpaceMember::getMemberType, 0)
                .eq(KbSpaceMember::getMemberId, userId)
                .eq(KbSpaceMember::getIsDelete, CommonConstant.UN_DELETE)
                .last("limit 1"));
        return m == null ? null : m.getRole();
    }
}
