package com.moli.knowledge.server.service;

import java.util.List;

/**
 * 知识库空间级 ACL（复用 user-center 的 Shiro 登录态）。
 *
 * <p>权限模型：
 * <ul>
 *   <li>读权限：仅 <b>负责人 owner_id</b>、<b>kb_space_member 已分配用户</b>、或平台超管（{@code superadmin}/{@code admin} / {@code *:*:*}）；
 *       未分配空间的登录用户不可见该空间及其文档/问答。</li>
 *   <li>visibility（公开/内部/私有）仅作空间元数据展示，<b>不</b>再自动授予读权限。</li>
 *   <li>成员 kb_space_member：member_type=0 用户 / 1 角色；role = viewer/editor/admin。</li>
 *   <li>负责人 owner_id：等同空间 admin。</li>
 *   <li>平台超管：{@link CommonConstant#hasFullPermission(String)} 或 Shiro {@code *:*:*}。</li>
 * </ul>
 *
 * <p>说明：当前 Dubbo 契约只透出权限串、不透出角色ID，故 <b>角色型成员(member_type=1)</b>
 * 暂按 best-effort 处理（运行时不解析，仅支持存储/管理）；用户型成员(member_type=0)完整生效。
 */
public interface KbAclService {

    /** 是否平台超管（superadmin/admin 或 Shiro *:*:*）。 */
    boolean isAdmin();

    /**
     * 是否具备空间管理页范围（菜单 {@code kb:space:admin} 或任一 {@code kb:space:*} 动作权限）。
     * 有此范围时，空间管理列表返回全部空间；具体操作仍由动作权限 + 断言控制。
     */
    boolean hasSpaceManageScope();

    /** 当前用户能否读该空间。 */
    boolean canRead(Long spaceId);

    /** 当前用户能否编辑该空间内容（editor/admin/owner/全局管理员）。 */
    boolean canEdit(Long spaceId);

    /** 能否管理该空间（成员/设置）：owner / 空间 admin / 全局管理员。 */
    boolean canAdmin(Long spaceId);

    /** 空间管理页列表中的可编辑标记（含 {@code kb:space:edit} 动作权限）。 */
    boolean manageListCanEdit(Long spaceId);

    /** 空间管理页列表中的可管理标记（含 {@code kb:space:member} / 菜单权限）。 */
    boolean manageListCanAdmin(Long spaceId);

    /** 读权限断言，失败抛 BaseException。 */
    void assertCanRead(Long spaceId);

    /** 编辑权限断言，失败抛 BaseException。 */
    void assertCanEdit(Long spaceId);

    /** 管理权限断言，失败抛 BaseException。 */
    void assertCanAdmin(Long spaceId);

    /** 读权限断言；有空间管理页范围时也可访问（供管理页加载详情）。 */
    void assertCanReadOrManageScope(Long spaceId);

    /** 编辑空间元数据断言（空间 admin 或 {@code kb:space:edit}）。 */
    void assertCanEditSpaceMeta(Long spaceId);

    /** 删除空间断言（空间 admin 或 {@code kb:space:remove}）。 */
    void assertCanRemoveSpace(Long spaceId);

    /** 当前用户可读的全部空间ID（用于列表/检索的统一过滤）。 */
    List<Long> accessibleSpaceIds();

    /** 当前用户可管理的空间ID（平台超管或有空间管理菜单/动作权限=全部；否则 owner / 空间 admin 成员）。 */
    List<Long> manageableSpaceIds();

    /**
     * 解析问答/检索的空间范围：{@code spaceIds} 非空优先，其次 {@code spaceId}，否则全部可读空间。
     * 对每个目标空间执行 {@link #assertCanRead(Long)}。
     */
    List<Long> resolveReadableSpaceIds(Long spaceId, List<Long> spaceIds);

    /** 按文档 ID 断言可读（查 kb_document.space_id 后校验空间权限）。 */
    void assertCanReadDocument(Long documentId);

    /** 按文档 ID 断言可编辑。 */
    void assertCanEditDocument(Long documentId);
}
