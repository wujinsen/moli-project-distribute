package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiImportBatchItemVo;
import com.moli.knowledge.server.dto.WikiImportBatchResultVo;
import com.moli.knowledge.server.dto.WikiImportResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /** T20c · 多文件导入，整批完成后一次 Sync。 */
    WikiImportBatchResultVo importBatch(Long spaceId,
                                        Long categoryId,
                                        List<MultipartFile> files,
                                        List<WikiImportBatchItemVo> items,
                                        String onConflict,
                                        boolean lintPreview,
                                        boolean sync);
}
