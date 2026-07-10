package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.constant.PermissionConstants;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.entity.KbSpaceMember;
import com.moli.knowledge.server.enums.SpaceVisibility;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.mapper.KbSpaceMemberMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbSpaceMemberMapper kbSpaceMemberMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;

    @Override
    public boolean isAdmin() {
        try {
            if (SecurityUtils.getSubject().isPermitted(PermissionConstants.SUPER_ADMIN)) {
                return true;
            }
            SysUser user = ShiroUtils.getUserInfo();
            if (user == null) {
                return false;
            }
            if (StringUtils.isNotBlank(user.getUserName())
                    && CommonConstant.hasFullPermission(user.getUserName())) {
                return true;
            }
            // Redis Session 里 SysUser 可能缺 userName，仍按种子 superadmin/admin 识别
            Long uid = user.getId();
            return Long.valueOf(1L).equals(uid) || Long.valueOf(2L).equals(uid);
        } catch (Exception e) {                            // 未登录/无 Subject
            return false;
        }
    }

    @Override
    public boolean hasSpaceManageScope() {
        if (isAdmin()) {
            return true;
        }
        try {
            return isPermitted(PermissionConstants.KB_SPACE_ADMIN)
                    || isPermitted(PermissionConstants.KB_SPACE_ADD)
                    || isPermitted(PermissionConstants.KB_SPACE_EDIT)
                    || isPermitted(PermissionConstants.KB_SPACE_REMOVE)
                    || isPermitted(PermissionConstants.KB_SPACE_MEMBER);
        } catch (Exception e) {
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
    public boolean manageListCanEdit(Long spaceId) {
        // 管理页按钮：由动作权限决定（菜单管数据、动作管按钮）
        return isAdmin() || isPermitted(PermissionConstants.KB_SPACE_EDIT);
    }

    @Override
    public boolean manageListCanAdmin(Long spaceId) {
        // 管理页“成员授权”按钮：由 kb:space:member 动作权限决定
        return isAdmin()
                || isPermitted(PermissionConstants.KB_SPACE_MEMBER)
                || isPermitted(PermissionConstants.KB_SPACE_ADMIN);
    }

    @Override
    public String resolveMySpaceRole(KbSpace space) {
        if (space == null) {
            return null;
        }
        if (isAdmin()) {
            return "platform";
        }
        Long userId = ShiroUtils.getUserId();
        if (userId != null && userId.equals(space.getOwnerId())) {
            return "owner";
        }
        String role = memberRole(space.getId(), userId);
        if (role != null) {
            return role;
        }
        if (hasSpaceManageScope()) {
            return "platform";
        }
        return null;
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
        if (canAdmin(spaceId)) {
            return;
        }
        if (isPermitted(PermissionConstants.KB_SPACE_MEMBER)
                || isPermitted(PermissionConstants.KB_SPACE_ADMIN)) {
            return;
        }
        throw new BaseException("无权管理该知识空间");
    }

    @Override
    public void assertCanReadOrManageScope(Long spaceId) {
        if (canRead(spaceId) || hasSpaceManageScope()) {
            return;
        }
        throw new BaseException("无权访问该知识空间");
    }

    @Override
    public void assertCanManageMembers(Long spaceId) {
        if (isAdmin() || isPermitted(PermissionConstants.KB_SPACE_MEMBER)) {
            return;
        }
        throw new BaseException("无权管理空间成员");
    }

    @Override
    public void assertCanEditSpaceMeta(Long spaceId) {
        // 空间信息编辑属管理操作：动作权限 kb:space:edit（或平台超管）
        if (isAdmin() || isPermitted(PermissionConstants.KB_SPACE_EDIT)) {
            return;
        }
        throw new BaseException("无权编辑该知识空间");
    }

    @Override
    public void assertCanRemoveSpace(Long spaceId) {
        if (isAdmin() || isPermitted(PermissionConstants.KB_SPACE_REMOVE)) {
            return;
        }
        throw new BaseException("无权删除该知识空间");
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
    public void assertPlatformLlmManage() {
        if (isAdmin() || isPermitted(PermissionConstants.KB_PLATFORM_LLM)) {
            return;
        }
        throw new BaseException("无权管理平台 LLM 配置");
    }

    @Override
    public void assertCanSyncTrigger(Long spaceId) {
        if (isAdmin()) {
            return;
        }
        if (spaceId == null) {
            throw new BaseException("无权触发全库同步");
        }
        if (isPermitted(PermissionConstants.KB_SYNC_TRIGGER)) {
            assertCanRead(spaceId);
            return;
        }
        assertCanAdmin(spaceId);
    }

    @Override
    public void assertCanSyncView(Long spaceId) {
        assertCanSyncTrigger(spaceId);
    }

    @Override
    public void assertCanLintScan(Long spaceId) {
        if (isAdmin()) {
            return;
        }
        if (isPermitted(PermissionConstants.KB_LINT_SCAN)) {
            if (spaceId != null) {
                assertCanRead(spaceId);
            }
            return;
        }
        if (spaceId != null) {
            assertCanEdit(spaceId);
            return;
        }
        throw new BaseException("全库体检需 kb:lint:scan 或全局管理员权限");
    }

    @Override
    public void assertCanRawUpload(Long spaceId) {
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        if (isAdmin()) {
            assertCanEdit(spaceId);
            return;
        }
        if (!isPermitted(PermissionConstants.KB_INGEST_RAW_UPLOAD)) {
            throw new BaseException("无权 Raw 投喂上传（需 kb:ingest:rawUpload）");
        }
        assertCanEdit(spaceId);
    }

    @Override
    public void assertCanOpsDashboard(Long spaceId) {
        if (isAdmin()) {
            return;
        }
        if (isPermitted(PermissionConstants.KB_OPS_DASHBOARD)) {
            if (spaceId != null) {
                assertCanRead(spaceId);
            }
            return;
        }
        if (isPermitted(PermissionConstants.KB_SYNC_TRIGGER)) {
            if (spaceId != null) {
                assertCanRead(spaceId);
            } else if (accessibleSpaceIds().isEmpty()) {
                throw new BaseException("无权查看运维 Dashboard");
            }
            return;
        }
        throw new BaseException("无权查看运维 Dashboard（需 kb:ops:dashboard 或 kb:sync:trigger）");
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

    @Override
    public List<Long> manageableSpaceIds() {
        List<KbSpace> spaces = kbSpaceMapper.selectList(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getIsDelete, CommonConstant.UN_DELETE));
        if (isAdmin() || hasSpaceManageScope()) {
            List<Long> ids = new ArrayList<>();
            for (KbSpace s : spaces) {
                ids.add(s.getId());
            }
            return ids;
        }
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        for (KbSpace s : spaces) {
            if (canAdmin(s.getId())) {
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
        // 公开空间：任意已登录用户可读（enterprise-kb 默认 visibility=公开）
        if (SpaceVisibility.PUBLIC.getCode() == nullSafeInt(space.getVisibility())) {
            return true;
        }
        // 未在 kb_space_member 分配的用户不可见；负责人默认可读
        if (userId.equals(space.getOwnerId())) {
            return true;
        }
        return memberRole(space.getId(), userId) != null;
    }

    private static int nullSafeInt(Integer value) {
        return value == null ? SpaceVisibility.INTERNAL.getCode() : value;
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

    private boolean isPermitted(String perm) {
        return SecurityUtils.getSubject().isPermitted(perm);
    }
}
