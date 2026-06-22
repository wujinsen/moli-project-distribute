package com.moli.knowledge.server.service;

import com.moli.knowledge.server.entity.KbAttachment;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface KbAttachmentService {

    KbAttachment upload(Long documentId, MultipartFile file);

    void download(Long id, HttpServletResponse response);

    void delete(Long id);
}
