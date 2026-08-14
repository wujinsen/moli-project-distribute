package com.moli.knowledge.server.service;

import com.moli.knowledge.server.entity.KbSpace;

import java.util.List;

/**
 * 知识库空间级 ACL（复用 user-center 的 Shiro 登录态）。
 *
 * <p>权限模型：
 * <ul>
 *   <li><b>内容侧（浏览/问答/文档）</b>：按空间成员角色控制——读=负责人/已分配成员/平台超管；
 *       写(canEdit)=成员 editor/admin、负责人、平台超管。未分配空间的登录用户不可见。</li>
 *   <li><b>管理侧（空间增删改、成员授权）</b>：由 RBAC 动作权限控制（{@code kb:space:add/edit/remove/member}）+
 *       平台超管；菜单 {@code kb:space:admin} 决定能否进入管理页并查看空间数据。<b>不</b>再叠加 per-space canAdmin。</li>
 *   <li>visibility：{@code 公开(2)} 对任意已登录用户可读；{@code 私有/内部} 仍须负责人或 kb_space_member。</li>
 *   <li>成员 kb_space_member：member_type=0 用户 / 1 角色；role = viewer/editor/admin。</li>
 *   <li>负责人 owner_id：等同空间 admin（内容侧）。</li>
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

    /**
     * 当前用户在该空间的成员角色（用于管理页展示，非操作按钮判定）。
     * 返回值：{@code platform} 平台超管 / {@code owner} 负责人 / {@code admin|editor|viewer} 成员角色。
     */
    String resolveMySpaceRole(KbSpace space);

    /** 读权限断言，失败抛 BaseException。 */
    void assertCanRead(Long spaceId);

    /** 编辑权限断言，失败抛 BaseException。 */
    void assertCanEdit(Long spaceId);

    /** 管理权限断言，失败抛 BaseException。 */
    void assertCanAdmin(Long spaceId);

    /** 成员授权断言：平台超管或具备 {@code kb:space:member} 动作权限。 */
    void assertCanManageMembers(Long spaceId);

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

    /** 平台 LLM 系统设置（超管或 {@code kb:platform:llm}）。 */
    void assertPlatformLlmManage();

    /**
     * 手动触发 Sync：平台超管、{@code kb:sync:trigger}（且可读该空间）、或空间 admin/owner。
     */
    void assertCanSyncTrigger(Long spaceId);

    /** 查看 Sync 日志/状态：与 {@link #assertCanSyncTrigger(Long)} 相同。 */
    void assertCanSyncView(Long spaceId);

    /**
     * 扫描并落库：平台超管、{@code kb:lint:scan}、或空间 editor（单空间 fallback）。
     * {@code spaceId=null} 表示全库扫描，需超管或 {@code kb:lint:scan}。
     */
    void assertCanLintScan(Long spaceId);

    /**
     * Raw 浏览器投喂：平台超管、{@code kb:ingest:rawUpload}，且具备空间 editor（T20d）。
     */
    void assertCanRawUpload(Long spaceId);

    /**
     * 运维 Dashboard：平台超管、{@code kb:ops:dashboard}，或具备 Sync 查看权限的空间范围。
     */
    void assertCanOpsDashboard(Long spaceId);
}
