package com.moli.user.center.server.api;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysNotice;
import com.moli.user.center.common.domain.vo.NoticeFeedVo;
import com.moli.user.center.common.domain.vo.NoticeVo;
import com.moli.user.center.server.controller.NoticeController;
import com.moli.user.center.server.mapper.NoticeMapper;
import com.moli.user.center.server.service.NoticeService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NoticeControllerApiTest extends AbstractApiTest {

    @InjectMocks
    private NoticeController controller;

    @Mock
    private NoticeMapper noticeMapper;

    @Mock
    private NoticeService noticeService;

    private SysNotice validNotice() {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("停机维护通知");
        notice.setNoticeType(3);
        return notice;
    }

    @Test
    public void GET_notice_list() {
        ControllerTestSupport.stubEmptyPage(noticeMapper);
        NoticeVo vo = new NoticeVo();
        vo.setPageNum(1);
        vo.setPageSize(10);
        ControllerTestSupport.assertSuccess(controller.list(vo));
    }

    @Test
    public void GET_notice_list_withFilters() {
        ControllerTestSupport.stubEmptyPage(noticeMapper);
        NoticeVo vo = new NoticeVo();
        vo.setPageNum(1);
        vo.setPageSize(10);
        vo.setNoticeTitle("维护");
        vo.setNoticeType(3);
        vo.setStatus(SysNotice.STATUS_PUBLISHED);
        ControllerTestSupport.assertSuccess(controller.list(vo));
    }

    @Test
    public void GET_notice_id() {
        ControllerTestSupport.stubSelectById(noticeMapper, new SysNotice());
        ControllerTestSupport.assertSuccess(controller.selectOne(1L));
    }

    /** 新建必须落草稿，且忽略前端传入的 status —— 否则「新建即全员可见」会绕过发布确认 */
    @Test
    public void POST_notice_insert_forcesDraftStatus() {
        ControllerTestSupport.stubInsert(noticeMapper);
        SysNotice notice = validNotice();
        notice.setStatus(SysNotice.STATUS_PUBLISHED);
        notice.setPublishTime(new Date());

        ControllerTestSupport.assertSuccess(controller.insert(notice));

        ArgumentCaptor<SysNotice> captor = ArgumentCaptor.forClass(SysNotice.class);
        verify(noticeMapper).insert(captor.capture());
        Assert.assertEquals(Integer.valueOf(SysNotice.STATUS_DRAFT), captor.getValue().getStatus());
        Assert.assertNull(captor.getValue().getPublishTime());
    }

    @Test(expected = BaseException.class)
    public void POST_notice_insert_rejectsBlankTitle() {
        SysNotice notice = validNotice();
        notice.setNoticeTitle("  ");
        controller.insert(notice);
    }

    @Test(expected = BaseException.class)
    public void POST_notice_insert_rejectsMissingType() {
        SysNotice notice = validNotice();
        notice.setNoticeType(null);
        controller.insert(notice);
    }

    /** 过期时间已是过去 = 存下来就永不可见，应在保存时就拦掉 */
    @Test(expected = BaseException.class)
    public void POST_notice_insert_rejectsExpireTimeInPast() {
        SysNotice notice = validNotice();
        notice.setExpireTime(new Date(1_000L));
        controller.insert(notice);
    }

    @Test
    public void POST_notice_insert_acceptsFutureExpireTime() {
        ControllerTestSupport.stubInsert(noticeMapper);
        SysNotice notice = validNotice();
        notice.setExpireTime(new Date(System.currentTimeMillis() + 86_400_000L));
        ControllerTestSupport.assertSuccess(controller.insert(notice));
    }

    /** 编辑不得改变发布状态：状态流转只走 publish / revoke 一条路径 */
    @Test
    public void PUT_notice_update_doesNotTouchStatus() {
        ControllerTestSupport.stubUpdate(noticeMapper);
        SysNotice notice = validNotice();
        notice.setId(1L);
        notice.setStatus(SysNotice.STATUS_PUBLISHED);

        ControllerTestSupport.assertSuccess(controller.update(notice));

        ArgumentCaptor<SysNotice> captor = ArgumentCaptor.forClass(SysNotice.class);
        verify(noticeMapper).updateById(captor.capture());
        Assert.assertNull(captor.getValue().getStatus());
        Assert.assertNull(captor.getValue().getPublishTime());
    }

    @Test(expected = BaseException.class)
    public void PUT_notice_update_rejectsMissingId() {
        controller.update(validNotice());
    }

    @Test
    public void PUT_notice_publish() {
        ControllerTestSupport.assertSuccess(controller.publish(1L));
        verify(noticeService).publish(1L);
    }

    @Test
    public void PUT_notice_revoke() {
        ControllerTestSupport.assertSuccess(controller.revoke(1L));
        verify(noticeService).revoke(1L);
    }

    @Test
    public void DELETE_notice_ids() {
        ControllerTestSupport.assertSuccess(controller.remove(new Long[]{1L, 2L}));
        verify(noticeMapper).deleteById(1L);
        verify(noticeMapper).deleteById(2L);
    }

    // ---------- 阅读侧 ----------

    @Test
    public void GET_notice_feed() {
        when(noticeService.feed()).thenReturn(new NoticeFeedVo());
        ControllerTestSupport.assertSuccess(controller.feed());
    }

    @Test
    public void GET_notice_feed_id() {
        when(noticeService.feedDetail(1L)).thenReturn(new SysNotice());
        ControllerTestSupport.assertSuccess(controller.feedDetail(1L));
    }

    @Test
    public void PUT_notice_feed_read() {
        ControllerTestSupport.assertSuccess(controller.markRead());
        verify(noticeService).markRead();
    }

}
