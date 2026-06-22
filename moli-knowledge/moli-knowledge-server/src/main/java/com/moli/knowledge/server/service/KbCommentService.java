package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.entity.KbComment;

public interface KbCommentService {

    Page<KbComment> page(Long documentId, int pageNum, int pageSize);

    Long create(KbComment comment);

    void delete(Long id);
}
