package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.dto.RawUploadItemVo;
import com.moli.knowledge.server.dto.RawUploadRenamedVo;
import com.moli.knowledge.server.dto.RawUploadResultVo;
import com.moli.knowledge.server.dto.RawUploadSkippedVo;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbRawUploadService;
import com.moli.knowledge.server.util.KbRawPathUtil;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class KbRawUploadServiceImpl implements KbRawUploadService {

    @Resource
    private KbIngestProperties ingestProperties;
    @Resource
    private KbAclService kbAclService;

    @Override
    public RawUploadResultVo upload(Long spaceId, String prefix, List<MultipartFile> files, String onConflict) {
        assertEnabled();
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        kbAclService.assertCanEdit(spaceId);
        if (files == null || files.isEmpty()) {
            throw new BaseException("请至少上传一个文件");
        }
        if (files.size() > ingestProperties.getRawUploadMaxFiles()) {
            throw new BaseException("单次最多上传 " + ingestProperties.getRawUploadMaxFiles() + " 个文件");
        }

        String cleanPrefix = KbRawPathUtil.normalizePrefix(prefix);
        String mode = normalizeOnConflict(onConflict);
        Path rawRoot = KbRawPathUtil.resolveRawRoot(ingestProperties.getRawRoot());

        RawUploadResultVo result = new RawUploadResultVo();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BaseException("空文件不允许上传");
            }
            if (file.getSize() > ingestProperties.getRawUploadMaxBytes()) {
                throw new BaseException("文件超过大小上限（"
                        + ingestProperties.getRawUploadMaxBytes() + " 字节）: " + file.getOriginalFilename());
            }
            String fileName = KbRawPathUtil.sanitizeFileName(file.getOriginalFilename());
            KbRawPathUtil.assertAllowedRawExtension(fileName);

            String relativePath = cleanPrefix + "/" + fileName;
            Path target = KbRawPathUtil.normalizeUnder(rawRoot, relativePath);
            boolean existed = Files.exists(target);

            if (existed) {
                if ("SKIP".equals(mode)) {
                    RawUploadSkippedVo skipped = new RawUploadSkippedVo();
                    skipped.setPath(relativePath);
                    skipped.setReason("ALREADY_EXISTS");
                    result.getSkipped().add(skipped);
                    continue;
                }
                if ("RENAME".equals(mode)) {
                    RenameResult renamed = resolveRenameTarget(rawRoot, cleanPrefix, fileName);
                    relativePath = renamed.relativePath;
                    target = renamed.target;
                    RawUploadRenamedVo renamedVo = new RawUploadRenamedVo();
                    renamedVo.setPath(relativePath);
                    renamedVo.setOriginalName(fileName);
                    result.getRenamed().add(renamedVo);
                }
            }

            try {
                if (target.getParent() != null) {
                    Files.createDirectories(target.getParent());
                }
                Files.write(target, file.getBytes());
            } catch (IOException e) {
                log.error("[raw-upload] write failed path={}", target, e);
                throw new BaseException("写入 raw 失败：" + e.getMessage());
            }

            log.info("[raw-upload] user={} spaceId={} path={} size={}",
                    ShiroUtils.getUserId(), spaceId, relativePath, file.getSize());

            RawUploadItemVo item = new RawUploadItemVo();
            item.setPath(relativePath);
            item.setSize(file.getSize());
            item.setOverwritten("OVERWRITE".equals(mode) && existed);
            result.getUploaded().add(item);
        }
        return result;
    }

    private void assertEnabled() {
        if (!ingestProperties.isEnabled()) {
            throw new BaseException("Ingest 工作台未启用");
        }
    }

    private static String normalizeOnConflict(String onConflict) {
        String mode = StringUtils.defaultIfBlank(onConflict, "SKIP").trim().toUpperCase(Locale.ROOT);
        if (!"SKIP".equals(mode) && !"OVERWRITE".equals(mode) && !"RENAME".equals(mode)) {
            throw new BaseException("onConflict 非法: " + onConflict);
        }
        return mode;
    }

    private RenameResult resolveRenameTarget(Path rawRoot, String prefix, String fileName) {
        for (int n = 1; n < 10000; n++) {
            String candidateName = KbRawPathUtil.renameWithSuffix(fileName, n);
            String relativePath = prefix + "/" + candidateName;
            Path target = KbRawPathUtil.normalizeUnder(rawRoot, relativePath);
            if (!Files.exists(target)) {
                RenameResult result = new RenameResult();
                result.relativePath = relativePath;
                result.target = target;
                return result;
            }
        }
        throw new BaseException("无法为文件生成唯一名称: " + fileName);
    }

    private static final class RenameResult {
        private String relativePath;
        private Path target;
    }
}
