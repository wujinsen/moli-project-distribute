package com.moli.knowledge.server.service;

import com.moli.knowledge.server.entity.KbAttachment;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface KbAttachmentService {

    List<KbAttachment> listByDocument(Long documentId);

    KbAttachment upload(Long documentId, MultipartFile file);

    void download(Long id, HttpServletResponse response);

    void delete(Long id);
}
