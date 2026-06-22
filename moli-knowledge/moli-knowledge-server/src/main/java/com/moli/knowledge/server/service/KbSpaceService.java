package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.entity.KbSpace;

public interface KbSpaceService {

    Page<KbSpace> page(KbSpace query, int pageNum, int pageSize);

    KbSpace getById(Long id);

    Long create(KbSpace space);

    void update(KbSpace space);

    void delete(Long id);
}
