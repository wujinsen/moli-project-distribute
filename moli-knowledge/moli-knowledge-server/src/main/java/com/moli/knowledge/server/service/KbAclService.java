package com.moli.knowledge.server.service;

import java.util.List;

/**
 * 知识库空间级 ACL（复用 user-center 的 Shiro 登录态）。
 *
 * <p>权限模型：
 * <ul>
 *   <li>读权限：仅 <b>负责人 owner_id</b>、<b>kb_space_member 已分配用户</b>、或全局 {@code kb:admin}；
 *       未分配空间的登录用户不可见该空间及其文档/问答。</li>
 *   <li>visibility（公开/内部/私有）仅作空间元数据展示，<b>不</b>再自动授予读权限。</li>
 *   <li>成员 kb_space_member：member_type=0 用户 / 1 角色；role = viewer/editor/admin。</li>
 *   <li>负责人 owner_id：等同空间 admin。</li>
 *   <li>全局管理员：Shiro 权限串 {@code kb:admin}（或通配 {@code *}）。</li>
 * </ul>
 *
 * <p>说明：当前 Dubbo 契约只透出权限串、不透出角色ID，故 <b>角色型成员(member_type=1)</b>
 * 暂按 best-effort 处理（运行时不解析，仅支持存储/管理）；用户型成员(member_type=0)完整生效。
 */
public interface KbAclService {

    /** 是否全局管理员（Shiro 权限 kb:admin / *）。 */
    boolean isAdmin();

    /** 当前用户能否读该空间。 */
    boolean canRead(Long spaceId);

    /** 当前用户能否编辑该空间内容（editor/admin/owner/全局管理员）。 */
    boolean canEdit(Long spaceId);

    /** 能否管理该空间（成员/设置）：owner / 空间 admin / 全局管理员。 */
    boolean canAdmin(Long spaceId);

    /** 读权限断言，失败抛 BaseException。 */
    void assertCanRead(Long spaceId);

    /** 编辑权限断言，失败抛 BaseException。 */
    void assertCanEdit(Long spaceId);

    /** 管理权限断言，失败抛 BaseException。 */
    void assertCanAdmin(Long spaceId);

    /** 当前用户可读的全部空间ID（用于列表/检索的统一过滤）。 */
    List<Long> accessibleSpaceIds();

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
