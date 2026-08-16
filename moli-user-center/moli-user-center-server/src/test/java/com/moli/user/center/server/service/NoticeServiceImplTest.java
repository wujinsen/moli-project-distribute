package com.moli.user.center.server.service;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysNotice;
import com.moli.user.center.common.domain.entity.SysNoticeReadCursor;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.vo.NoticeFeedVo;
import com.moli.user.center.server.config.util.ShiroUtils;
import com.moli.user.center.server.mapper.NoticeMapper;
import com.moli.user.center.server.mapper.NoticeReadCursorMapper;
import com.moli.user.center.server.service.impl.NoticeServiceImpl;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 发布/撤回状态机 + 未读水位。
 *
 * <p>水位语义（「publish_time 晚于水位即未读」）是设计里唯一取了巧的地方，
 * 因此这里覆盖首次登录无水位、部分已读、全部已读三种情形。
 */
@RunWith(MockitoJUnitRunner.class)
public class NoticeServiceImplTest extends AbstractApiTest {

    private static final long USER_ID = 1001L;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    @Mock
    private NoticeMapper noticeMapper;

    @Mock
    private NoticeReadCursorMapper noticeReadCursorMapper;

    private MockedStatic<ShiroUtils> stubCurrentUser() {
        SysUser user = new SysUser();
        user.setId(USER_ID);
        MockedStatic<ShiroUtils> mocked = mockStatic(ShiroUtils.class);
        mocked.when(ShiroUtils::getUserInfo).thenReturn(user);
        return mocked;
    }

    private SysNotice notice(long id, int status, Date publishTime) {
        SysNotice notice = new SysNotice();
        notice.setId(id);
        notice.setStatus(status);
        notice.setTopFlag(0);
        notice.setPublishTime(publishTime);
        return notice;
    }

    // ---------- 发布 ----------

    @Test
    public void publish_setsPublishedStatusAndPublishTime() {
        when(noticeMapper.selectById(1L)).thenReturn(notice(1L, SysNotice.STATUS_DRAFT, null));
        when(noticeMapper.updateById(any())).thenReturn(1);

        noticeService.publish(1L);

        ArgumentCaptor<SysNotice> captor = ArgumentCaptor.forClass(SysNotice.class);
        verify(noticeMapper).updateById(captor.capture());
        Assert.assertEquals(Integer.valueOf(SysNotice.STATUS_PUBLISHED), captor.getValue().getStatus());
        Assert.assertNotNull(captor.getValue().getPublishTime());
    }

    /** 已撤回的公告可以重新发布 */
    @Test
    public void publish_allowedFromRevoked() {
        when(noticeMapper.selectById(1L)).thenReturn(notice(1L, SysNotice.STATUS_REVOKED, new Date()));
        when(noticeMapper.updateById(any())).thenReturn(1);

        noticeService.publish(1L);

        verify(noticeMapper).updateById(any(SysNotice.class));
    }

    /** 重复发布会刷新 publish_time，把旧公告顶成全员未读，故拒绝 */
    @Test(expected = BaseException.class)
    public void publish_rejectsAlreadyPublished() {
        when(noticeMapper.selectById(1L)).thenReturn(notice(1L, SysNotice.STATUS_PUBLISHED, new Date()));
        noticeService.publish(1L);
    }

    @Test(expected = BaseException.class)
    public void publish_rejectsMissingNotice() {
        when(noticeMapper.selectById(9L)).thenReturn(null);
        noticeService.publish(9L);
    }

    @Test(expected = BaseException.class)
    public void publish_rejectsNullId() {
        noticeService.publish(null);
    }

    /** 草稿搁置太久、过期时间已过：发布出去在阅读侧一开始就不可见，应拦住 */
    @Test(expected = BaseException.class)
    public void publish_rejectsAlreadyExpiredNotice() {
        SysNotice stale = notice(1L, SysNotice.STATUS_DRAFT, null);
        stale.setExpireTime(new Date(1_000L));
        when(noticeMapper.selectById(1L)).thenReturn(stale);
        noticeService.publish(1L);
    }

    // ---------- 撤回 ----------

    @Test
    public void revoke_setsRevokedStatus() {
        when(noticeMapper.selectById(1L)).thenReturn(notice(1L, SysNotice.STATUS_PUBLISHED, new Date()));
        when(noticeMapper.updateById(any())).thenReturn(1);

        noticeService.revoke(1L);

        ArgumentCaptor<SysNotice> captor = ArgumentCaptor.forClass(SysNotice.class);
        verify(noticeMapper).updateById(captor.capture());
        Assert.assertEquals(Integer.valueOf(SysNotice.STATUS_REVOKED), captor.getValue().getStatus());
    }

    @Test(expected = BaseException.class)
    public void revoke_rejectsDraft() {
        when(noticeMapper.selectById(1L)).thenReturn(notice(1L, SysNotice.STATUS_DRAFT, null));
        noticeService.revoke(1L);
    }

    // ---------- 阅读侧未读水位 ----------

    /** 首次登录（无水位行）时，所有有效公告都算未读 */
    @Test
    public void feed_noCursor_countsAllAsUnread() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            when(noticeMapper.selectList(any())).thenReturn(Arrays.asList(
                    notice(1L, SysNotice.STATUS_PUBLISHED, new Date(1_000L)),
                    notice(2L, SysNotice.STATUS_PUBLISHED, new Date(2_000L))));
            when(noticeReadCursorMapper.selectById(USER_ID)).thenReturn(null);

            NoticeFeedVo feed = noticeService.feed();

            Assert.assertEquals(2, feed.getList().size());
            Assert.assertEquals(Integer.valueOf(2), feed.getUnreadCount());
            feed.getList().forEach(it -> Assert.assertTrue(it.getUnread()));
        }
    }

    /** 水位落在两条公告之间：只有更晚发布的那条算未读 */
    @Test
    public void feed_withCursor_countsOnlyNewerAsUnread() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            when(noticeMapper.selectList(any())).thenReturn(Arrays.asList(
                    notice(1L, SysNotice.STATUS_PUBLISHED, new Date(3_000L)),
                    notice(2L, SysNotice.STATUS_PUBLISHED, new Date(1_000L))));
            SysNoticeReadCursor cursor = new SysNoticeReadCursor();
            cursor.setUserId(USER_ID);
            cursor.setLastReadTime(new Date(2_000L));
            when(noticeReadCursorMapper.selectById(USER_ID)).thenReturn(cursor);

            NoticeFeedVo feed = noticeService.feed();

            Assert.assertEquals(Integer.valueOf(1), feed.getUnreadCount());
            Assert.assertTrue(feed.getList().get(0).getUnread());
            Assert.assertFalse(feed.getList().get(1).getUnread());
        }
    }

    @Test
    public void feed_cursorNewerThanAll_countsZeroUnread() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            when(noticeMapper.selectList(any())).thenReturn(Collections.singletonList(
                    notice(1L, SysNotice.STATUS_PUBLISHED, new Date(1_000L))));
            SysNoticeReadCursor cursor = new SysNoticeReadCursor();
            cursor.setUserId(USER_ID);
            cursor.setLastReadTime(new Date(5_000L));
            when(noticeReadCursorMapper.selectById(USER_ID)).thenReturn(cursor);

            NoticeFeedVo feed = noticeService.feed();

            Assert.assertEquals(Integer.valueOf(0), feed.getUnreadCount());
            Assert.assertFalse(feed.getList().get(0).getUnread());
        }
    }

    /** 列表项不携带正文，避免首页请求被长 Markdown 撑大 */
    @Test
    public void feed_briefItemsCarryNoContent() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            SysNotice withContent = notice(1L, SysNotice.STATUS_PUBLISHED, new Date());
            withContent.setNoticeContent("# 很长的 Markdown 正文");
            when(noticeMapper.selectList(any())).thenReturn(Collections.singletonList(withContent));
            when(noticeReadCursorMapper.selectById(USER_ID)).thenReturn(null);

            NoticeFeedVo feed = noticeService.feed();

            // NoticeBriefVo 没有 content 字段，此处校验的是「响应体不含正文」这一契约
            Assert.assertEquals(1, feed.getList().size());
            Assert.assertEquals(Long.valueOf(1L), feed.getList().get(0).getId());
        }
    }

    // ---------- 阅读侧详情 ----------

    /** 草稿/已撤回在阅读侧不可见，且不区分「不存在」以免被探测 */
    @Test(expected = BaseException.class)
    public void feedDetail_rejectsInvisibleNotice() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            when(noticeMapper.selectOne(any())).thenReturn(null);
            noticeService.feedDetail(1L);
        }
    }

    @Test
    public void feedDetail_returnsPublishedNotice() {
        SysNotice published = notice(1L, SysNotice.STATUS_PUBLISHED, new Date());
        when(noticeMapper.selectOne(any())).thenReturn(published);

        Assert.assertEquals(Long.valueOf(1L), noticeService.feedDetail(1L).getId());
    }

    // ---------- 标记已读 ----------

    @Test
    public void markRead_insertsCursorOnFirstRead() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            when(noticeReadCursorMapper.selectById(USER_ID)).thenReturn(null);
            when(noticeReadCursorMapper.insert(any())).thenReturn(1);

            noticeService.markRead();

            ArgumentCaptor<SysNoticeReadCursor> captor = ArgumentCaptor.forClass(SysNoticeReadCursor.class);
            verify(noticeReadCursorMapper).insert(captor.capture());
            Assert.assertEquals(Long.valueOf(USER_ID), captor.getValue().getUserId());
            Assert.assertNotNull(captor.getValue().getLastReadTime());
        }
    }

    @Test
    public void markRead_updatesExistingCursor() {
        try (MockedStatic<ShiroUtils> ignored = stubCurrentUser()) {
            SysNoticeReadCursor cursor = new SysNoticeReadCursor();
            cursor.setUserId(USER_ID);
            cursor.setLastReadTime(new Date(1_000L));
            when(noticeReadCursorMapper.selectById(USER_ID)).thenReturn(cursor);
            when(noticeReadCursorMapper.updateById(any())).thenReturn(1);

            noticeService.markRead();

            verify(noticeReadCursorMapper).updateById(any(SysNoticeReadCursor.class));
            verify(noticeReadCursorMapper, never()).insert(any());
        }
    }

    @Test(expected = BaseException.class)
    public void markRead_rejectsWhenNoLoginUser() {
        try (MockedStatic<ShiroUtils> mocked = mockStatic(ShiroUtils.class)) {
            mocked.when(ShiroUtils::getUserInfo).thenReturn(null);
            noticeService.markRead();
        }
    }
}
