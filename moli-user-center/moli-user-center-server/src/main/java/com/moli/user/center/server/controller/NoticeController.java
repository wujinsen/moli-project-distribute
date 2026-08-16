package com.moli.user.center.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.SysNotice;
import com.moli.user.center.common.domain.vo.NoticeFeedVo;
import com.moli.user.center.common.domain.vo.NoticeVo;
import com.moli.user.center.server.mapper.NoticeMapper;
import com.moli.user.center.server.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 通知公告。设计见 {@code docs/design/sys-config-notice.md} §4。
 *
 * <p><b>后台管理侧</b>（{@code /notice/*}）要求 {@code system:notice:*} 权限，可见全部状态；
 * <b>阅读侧</b>（{@code /notice/feed*}）仅要求登录，只可见已发布且未过期的公告。
 *
 * <p>两侧用路径前缀分开，而不是在同一个接口里按角色切换可见性——
 * 混在一起迟早会写出把草稿泄露给全员的分支。
 */
@RestController
@RequestMapping("notice")
@Api(tags = "通知公告")
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeService noticeService;

    // ==================== 后台管理侧 ====================

    /**
     * 后台公告列表。含草稿与已撤回；**不返回正文**，避免列表响应被长 Markdown 撑大。
     */
    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.SYSTEM_NOTICE_LIST)
    @ApiOperation(value = "公告列表", notes = "后台管理列表，含草稿/已撤回，不含正文")
    public MoliResult<PageRes<SysNotice>> list(NoticeVo noticeVo) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .select(SysNotice::getId, SysNotice::getNoticeTitle, SysNotice::getNoticeType,
                        SysNotice::getStatus, SysNotice::getTopFlag, SysNotice::getPublishTime,
                        SysNotice::getExpireTime, SysNotice::getCreateId, SysNotice::getCreateTime,
                        SysNotice::getUpdateId, SysNotice::getUpdateTime);
        if (StringUtils.isNotBlank(noticeVo.getNoticeTitle())) {
            wrapper.like(SysNotice::getNoticeTitle, noticeVo.getNoticeTitle());
        }
        if (noticeVo.getNoticeType() != null) {
            wrapper.eq(SysNotice::getNoticeType, noticeVo.getNoticeType());
        }
        if (noticeVo.getStatus() != null) {
            wrapper.eq(SysNotice::getStatus, noticeVo.getStatus());
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);

        Page<SysNotice> page = new Page<>();
        page.setCurrent(noticeVo.getPageNum());
        page.setSize(noticeVo.getPageSize());
        noticeMapper.selectPage(page, wrapper);

        PageRes<SysNotice> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setList(page.getRecords());
        result.setPageNum(noticeVo.getPageNum());
        result.setPageSize(noticeVo.getPageSize());
        return MoliResult.success(result);
    }

    @GetMapping("/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_NOTICE_LIST)
    @ApiOperation(value = "公告详情", notes = "后台详情，任意状态可见，含正文")
    public MoliResult<SysNotice> selectOne(@PathVariable Long id) {
        return MoliResult.success(noticeMapper.selectById(id));
    }

    /**
     * 新增公告。强制落草稿状态：状态由发布/撤回接口驱动，
     * 不接受前端直接指定，否则「新建即全员可见」会绕过发布这一道确认。
     */
    @PostMapping
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_NOTICE_ADD, PermissionConstants.SYSTEM_NOTICE_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "新增公告", notes = "落草稿状态，需再调发布接口才对外可见")
    public MoliResult<Long> insert(@RequestBody SysNotice notice) {
        validateForSave(notice);
        notice.setStatus(SysNotice.STATUS_DRAFT);
        notice.setPublishTime(null);
        noticeMapper.insert(notice);
        return MoliResult.success(notice.getId());
    }

    /**
     * 修改公告。同样忽略前端传入的 status / publishTime——状态流转只走发布/撤回接口，
     * 保证「什么时候变成对外可见」只有一条路径。
     */
    @PutMapping
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_NOTICE_EDIT, PermissionConstants.SYSTEM_NOTICE_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "修改公告", notes = "不改变发布状态；状态流转请用 publish / revoke")
    public MoliResult<Boolean> update(@RequestBody SysNotice notice) {
        if (notice.getId() == null) {
            throw new BaseException("公告 ID 不能为空");
        }
        validateForSave(notice);
        notice.setStatus(null);
        notice.setPublishTime(null);
        noticeMapper.updateById(notice);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping("/publish/{id}")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_NOTICE_EDIT, PermissionConstants.SYSTEM_NOTICE_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "发布公告", notes = "置为已发布并写入发布时间，阅读侧立即可见")
    public MoliResult<Boolean> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping("/revoke/{id}")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_NOTICE_EDIT, PermissionConstants.SYSTEM_NOTICE_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "撤回公告", notes = "置为已撤回，阅读侧立即不可见；保留发布痕迹")
    public MoliResult<Boolean> revoke(@PathVariable Long id) {
        noticeService.revoke(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{ids}")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_NOTICE_REMOVE, PermissionConstants.SYSTEM_NOTICE_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "批量删除公告", notes = "物理删除；仅用于清理误建草稿，撤回请用 revoke")
    public MoliResult<Boolean> remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            noticeMapper.deleteById(id);
        }
        return MoliResult.success(Boolean.TRUE);
    }

    // ==================== 阅读侧（仅需登录）====================

    /**
     * 当前有效公告 + 未读数。公告的意义是全员可见，因此这里不挂 perms。
     */
    @GetMapping("/feed")
    @ApiOperation(value = "有效公告与未读数", notes = "已发布且未过期，置顶优先；仅需登录")
    public MoliResult<NoticeFeedVo> feed() {
        return MoliResult.success(noticeService.feed());
    }

    @GetMapping("/feed/{id}")
    @ApiOperation(value = "公告详情（阅读侧）", notes = "仅已发布且未过期可见；仅需登录")
    public MoliResult<SysNotice> feedDetail(@PathVariable Long id) {
        return MoliResult.success(noticeService.feedDetail(id));
    }

    @PutMapping("/feed/read")
    @ApiOperation(value = "标记已读", notes = "把当前用户的已读水位推进到此刻")
    public MoliResult<Boolean> markRead() {
        noticeService.markRead();
        return MoliResult.success(Boolean.TRUE);
    }

    private void validateForSave(SysNotice notice) {
        if (StringUtils.isBlank(notice.getNoticeTitle())) {
            throw new BaseException("公告标题不能为空");
        }
        if (notice.getNoticeType() == null) {
            throw new BaseException("公告类型不能为空");
        }
        // 不拿 expireTime 与入参 publishTime 比：发布时间由发布动作赋值，入参里的会被丢弃。
        // 真正有意义的约束是过期时间不能已经过去，否则存下来就是一条永不可见的公告。
        if (notice.getExpireTime() != null && notice.getExpireTime().before(new Date())) {
            throw new BaseException("过期时间不能早于当前时间");
        }
    }

}
