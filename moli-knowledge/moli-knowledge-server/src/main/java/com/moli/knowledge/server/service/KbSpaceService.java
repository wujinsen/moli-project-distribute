package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.dto.KbAccessibleSpaceVo;
import com.moli.knowledge.server.entity.KbSpace;

import java.util.List;

public interface KbSpaceService {

    Page<KbSpace> page(KbSpace query, int pageNum, int pageSize);

    /** 当前用户可读空间列表（含 canEdit/canAdmin，供前端空间选择器）。 */
    List<KbAccessibleSpaceVo> listAccessible();

    /** 当前用户可管理空间列表（供空间管理页；平台超管=全部）。 */
    List<KbAccessibleSpaceVo> listManageable();

    KbSpace getById(Long id);

    Long create(KbSpace space);

    void update(KbSpace space);

    void delete(Long id);
}
