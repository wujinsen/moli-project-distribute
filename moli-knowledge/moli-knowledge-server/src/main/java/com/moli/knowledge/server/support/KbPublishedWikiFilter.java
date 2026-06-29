package com.moli.knowledge.server.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.enums.DocumentStatus;

/**
 * 浏览与管理共用的 wiki 已发布文档过滤（source=kb + status=published）。
 */
public final class KbPublishedWikiFilter {

    public static final String SOURCE_KB = "kb";

    private KbPublishedWikiFilter() {
    }

    public static LambdaQueryWrapper<KbDocument> publishedKbWrapper(Long spaceId) {
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<>();
        w.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        w.eq(KbDocument::getStatus, DocumentStatus.PUBLISHED.getCode());
        w.eq(KbDocument::getSource, SOURCE_KB);
        if (spaceId != null) {
            w.eq(KbDocument::getSpaceId, spaceId);
        }
        return w;
    }

    public static QueryWrapper<KbDocument> publishedKbQuery(Long spaceId) {
        QueryWrapper<KbDocument> qw = new QueryWrapper<>();
        qw.eq("is_delete", CommonConstant.UN_DELETE);
        qw.eq("status", DocumentStatus.PUBLISHED.getCode());
        qw.eq("source", SOURCE_KB);
        if (spaceId != null) {
            qw.eq("space_id", spaceId);
        }
        return qw;
    }
}
