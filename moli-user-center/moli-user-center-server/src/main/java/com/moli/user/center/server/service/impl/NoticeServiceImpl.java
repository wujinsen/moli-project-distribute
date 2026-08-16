package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysNotice;
import com.moli.user.center.common.domain.entity.SysNoticeReadCursor;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.vo.NoticeBriefVo;
import com.moli.user.center.common.domain.vo.NoticeFeedVo;
import com.moli.user.center.server.config.util.ShiroUtils;
import com.moli.user.center.server.mapper.NoticeMapper;
import com.moli.user.center.server.mapper.NoticeReadCursorMapper;
import com.moli.user.center.server.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeReadCursorMapper noticeReadCursorMapper;

    @Override
    public void publish(Long id) {
        SysNotice notice = requireExists(id);
        if (SysNotice.STATUS_PUBLISHED == notice.getStatus()) {
            // 重复发布会刷新 publish_time，使旧公告在所有人的通知栏里重新变成未读
            throw new BaseException("公告已处于发布状态，如需重新推送请先撤回");
        }
        Date now = new Date();
        if (notice.getExpireTime() != null && notice.getExpireTime().before(now)) {
            // 草稿可能搁置很久，此时过期时间已成过去，发布出去在阅读侧一开始就不可见
            throw new BaseException("公告过期时间已过，请先修改过期时间再发布");
        }
        SysNotice update = new SysNotice();
        update.setId(id);
        update.setStatus(SysNotice.STATUS_PUBLISHED);
        update.setPublishTime(now);
        noticeMapper.updateById(update);
        log.info("公告 {} 已发布", id);
    }

    @Override
    public void revoke(Long id) {
        SysNotice notice = requireExists(id);
        if (SysNotice.STATUS_PUBLISHED != notice.getStatus()) {
            throw new BaseException("只有已发布的公告才能撤回");
        }
        SysNotice update = new SysNotice();
        update.setId(id);
        update.setStatus(SysNotice.STATUS_REVOKED);
        noticeMapper.updateById(update);
        log.info("公告 {} 已撤回", id);
    }

    @Override
    public NoticeFeedVo feed() {
        Date now = new Date();
        List<SysNotice> effective = noticeMapper.selectList(effectiveWrapper(now)
                .orderByDesc(SysNotice::getTopFlag)
                .orderByDesc(SysNotice::getPublishTime));

        Date cursor = currentCursor();
        List<NoticeBriefVo> list = new ArrayList<>(effective.size());
        int unreadCount = 0;
        for (SysNotice notice : effective) {
            boolean unread = cursor == null
                    || (notice.getPublishTime() != null && notice.getPublishTime().after(cursor));
            if (unread) {
                unreadCount++;
            }
            NoticeBriefVo vo = new NoticeBriefVo();
            vo.setId(notice.getId());
            vo.setNoticeTitle(notice.getNoticeTitle());
            vo.setNoticeType(notice.getNoticeType());
            vo.setTopFlag(notice.getTopFlag());
            vo.setPublishTime(notice.getPublishTime());
            vo.setExpireTime(notice.getExpireTime());
            vo.setUnread(unread);
            list.add(vo);
        }

        NoticeFeedVo feedVo = new NoticeFeedVo();
        feedVo.setList(list);
        feedVo.setUnreadCount(unreadCount);
        return feedVo;
    }

    @Override
    public SysNotice feedDetail(Long id) {
        SysNotice notice = noticeMapper.selectOne(effectiveWrapper(new Date())
                .eq(SysNotice::getId, id));
        if (notice == null) {
            // 不区分「不存在」与「未发布/已撤回」，避免通过阅读侧接口探测草稿是否存在
            throw new BaseException("公告不存在或已不可见");
        }
        return notice;
    }

    @Override
    public void markRead() {
        Long userId = currentUserId();
        Date now = new Date();
        SysNoticeReadCursor existing = noticeReadCursorMapper.selectById(userId);
        if (existing == null) {
            SysNoticeReadCursor cursor = new SysNoticeReadCursor();
            cursor.setUserId(userId);
            cursor.setLastReadTime(now);
            cursor.setUpdateTime(now);
            noticeReadCursorMapper.insert(cursor);
        } else {
            existing.setLastReadTime(now);
            existing.setUpdateTime(now);
            noticeReadCursorMapper.updateById(existing);
        }
    }

    /**
     * 「当前有效」= 已发布 且（无过期时间 或 未过期）。
     *
     * <p>抽成方法供 feed 与 feedDetail 共用：两处若各写一份条件，
     * 迟早会出现列表看不到但详情能打开（或反之）的不一致。
     */
    private LambdaQueryWrapper<SysNotice> effectiveWrapper(Date now) {
        return new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, SysNotice.STATUS_PUBLISHED)
                .and(w -> w.isNull(SysNotice::getExpireTime).or().gt(SysNotice::getExpireTime, now));
    }

    private Date currentCursor() {
        SysNoticeReadCursor cursor = noticeReadCursorMapper.selectById(currentUserId());
        return cursor == null ? null : cursor.getLastReadTime();
    }

    private Long currentUserId() {
        SysUser user = ShiroUtils.getUserInfo();
        if (user == null || user.getId() == null) {
            throw new BaseException("未获取到当前登录用户");
        }
        return user.getId();
    }

    private SysNotice requireExists(Long id) {
        if (id == null) {
            throw new BaseException("公告 ID 不能为空");
        }
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BaseException("公告不存在");
        }
        return notice;
    }
}
