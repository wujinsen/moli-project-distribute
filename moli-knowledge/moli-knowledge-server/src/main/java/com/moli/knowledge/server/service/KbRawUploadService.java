package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.RawUploadResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KbRawUploadService {

    RawUploadResultVo upload(Long spaceId, String prefix, List<MultipartFile> files, String onConflict);

    /** T20c · zip 解压投喂到 raw 目录。 */
    RawUploadResultVo uploadZip(Long spaceId, String prefix, MultipartFile zipFile, String onConflict);
}
