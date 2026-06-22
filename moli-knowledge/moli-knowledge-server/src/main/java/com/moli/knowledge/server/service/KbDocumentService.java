package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.dto.DocumentDetailVo;
import com.moli.knowledge.server.dto.DocumentSaveRequest;
import com.moli.knowledge.server.dto.DocumentSearchRequest;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentVersion;

public interface KbDocumentService {

    Page<KbDocument> search(DocumentSearchRequest request);

    DocumentDetailVo detail(Long id);

    Long save(DocumentSaveRequest request);

    void publish(Long id);

    void archive(Long id);

    void delete(Long id);

    Page<KbDocumentVersion> versions(Long documentId, int pageNum, int pageSize);
}
