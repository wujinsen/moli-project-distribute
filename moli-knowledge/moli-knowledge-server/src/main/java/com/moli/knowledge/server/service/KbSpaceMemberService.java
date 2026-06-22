package com.moli.knowledge.server.service;

import com.moli.knowledge.server.entity.KbSpaceMember;

import java.util.List;

public interface KbSpaceMemberService {

    /** 列出空间成员（需空间管理权限）。 */
    List<KbSpaceMember> list(Long spaceId);

    /** 添加成员（需空间管理权限）。 */
    Long add(KbSpaceMember member);

    /** 更新成员角色（需空间管理权限）。 */
    void update(KbSpaceMember member);

    /** 移除成员（需空间管理权限）。 */
    void remove(Long id);
}
