package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.entity.KbDocument;

public interface KbFavoriteService {

    void add(Long documentId);

    void remove(Long documentId);

    Page<KbDocument> myFavorites(int pageNum, int pageSize);
}
