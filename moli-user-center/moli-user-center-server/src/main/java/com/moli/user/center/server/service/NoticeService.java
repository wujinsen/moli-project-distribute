package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.entity.SysNotice;
import com.moli.user.center.common.domain.vo.NoticeFeedVo;

/**
 * 通知公告：发布/撤回状态机 + 阅读侧水位。
 *
 * <p>纯 CRUD 部分（分页、详情、删除）留在 Controller 直调 Mapper，与 {@code PostController} 一致；
 * 本接口只承载有实际逻辑的部分，不为对称而转发 Mapper。
 */
public interface NoticeService {

    /**
     * 发布：置 {@code status=1} 并写入 {@code publish_time}。
     *
     * <p>重复发布已发布的公告会刷新 {@code publish_time}，这会让它在阅读侧重新变成未读——
     * 因此仅允许从草稿或已撤回状态发布，避免误操作把旧公告顶成新的。
     */
    void publish(Long id);

    /**
     * 撤回：置 {@code status=2}，阅读侧立即不可见。
     */
    void revoke(Long id);

    /**
     * 阅读侧：当前有效公告 + 未读数。有效 = 已发布且未过期。
     */
    NoticeFeedVo feed();

    /**
     * 阅读侧详情：**仅**返回已发布且未过期的公告，草稿与已撤回不可见。
     */
    SysNotice feedDetail(Long id);

    /**
     * 把当前用户的已读水位推进到此刻。
     */
    void markRead();
}
