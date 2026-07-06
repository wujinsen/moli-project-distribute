package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.RawUploadResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KbRawUploadService {

    RawUploadResultVo upload(Long spaceId, String prefix, List<MultipartFile> files, String onConflict);
}
