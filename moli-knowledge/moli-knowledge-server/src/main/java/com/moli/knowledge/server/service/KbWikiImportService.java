package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiImportResultVo;
import org.springframework.web.multipart.MultipartFile;

public interface KbWikiImportService {

    WikiImportResultVo importPage(Long spaceId,
                                  Long categoryId,
                                  MultipartFile file,
                                  String slug,
                                  String title,
                                  String onConflict,
                                  boolean lintPreview,
                                  boolean sync,
                                  MultipartFile assetsZip);
}
