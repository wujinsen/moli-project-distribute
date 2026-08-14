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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        kbAclService.assertCanRawUpload(spaceId);
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
            writeRawBytes(rawRoot, cleanPrefix, fileName, readBytes(file), mode, result);
        }
        return result;
    }

    @Override
    public RawUploadResultVo uploadZip(Long spaceId, String prefix, MultipartFile zipFile, String onConflict) {
        assertEnabled();
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        kbAclService.assertCanRawUpload(spaceId);
        if (zipFile == null || zipFile.isEmpty()) {
            throw new BaseException("请上传 zip 文件");
        }
        if (zipFile.getSize() > ingestProperties.getRawUploadZipMaxBytes()) {
            throw new BaseException("zip 超过大小上限（"
                    + ingestProperties.getRawUploadZipMaxBytes() + " 字节）");
        }
        String originalName = zipFile.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new BaseException("仅支持 .zip 文件");
        }

        String cleanPrefix = KbRawPathUtil.normalizePrefix(prefix);
        String mode = normalizeOnConflict(onConflict);
        Path rawRoot = KbRawPathUtil.resolveRawRoot(ingestProperties.getRawRoot());
        RawUploadResultVo result = new RawUploadResultVo();

        int entries = 0;
        try (InputStream in = zipFile.getInputStream();
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                entries++;
                if (entries > ingestProperties.getRawUploadZipMaxEntries()) {
                    throw new BaseException("zip 内文件数超过上限 "
                            + ingestProperties.getRawUploadZipMaxEntries());
                }
                String entryName = normalizeZipEntryName(entry.getName());
                if (entryName.isEmpty()) {
                    continue;
                }
                String fileName = PathsLeaf(entryName);
                KbRawPathUtil.assertAllowedRawExtension(fileName);
                byte[] data = readZipEntry(zis);
                if (data.length == 0) {
                    throw new BaseException("zip 内空文件不允许: " + entryName);
                }
                if (data.length > ingestProperties.getRawUploadMaxBytes()) {
                    throw new BaseException("zip 内文件超过单文件上限: " + entryName);
                }
                String relativeWithinPrefix = entryName.contains("/")
                        ? entryName.substring(0, entryName.lastIndexOf('/')) + "/" + fileName
                        : fileName;
                writeRawBytes(rawRoot, cleanPrefix, relativeWithinPrefix, data, mode, result);
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("[raw-upload-zip] failed", e);
            throw new BaseException("解压 zip 失败：" + e.getMessage());
        }
        if (result.getUploaded().isEmpty() && result.getSkipped().isEmpty() && result.getRenamed().isEmpty()) {
            throw new BaseException("zip 内无有效 md/txt 文件");
        }
        return result;
    }

    private void writeRawBytes(Path rawRoot, String prefix, String relativeName, byte[] data,
                               String mode, RawUploadResultVo result) {
        String fileName = relativeName.contains("/")
                ? relativeName.substring(relativeName.lastIndexOf('/') + 1)
                : relativeName;
        KbRawPathUtil.assertAllowedRawExtension(fileName);

        String relativePath = prefix + "/" + relativeName.replace('\\', '/');
        while (relativePath.contains("//")) {
            relativePath = relativePath.replace("//", "/");
        }
        Path target = KbRawPathUtil.normalizeUnder(rawRoot, relativePath);
        boolean existed = Files.exists(target);

        if (existed) {
            if ("SKIP".equals(mode)) {
                RawUploadSkippedVo skipped = new RawUploadSkippedVo();
                skipped.setPath(relativePath);
                skipped.setReason("ALREADY_EXISTS");
                result.getSkipped().add(skipped);
                return;
            }
            if ("RENAME".equals(mode)) {
                RenameResult renamed = resolveRenameTarget(rawRoot, prefix,
                        relativeName.contains("/") ? relativeName : fileName);
                relativePath = renamed.relativePath;
                target = renamed.target;
                RawUploadRenamedVo renamedVo = new RawUploadRenamedVo();
                renamedVo.setPath(relativePath);
                renamedVo.setOriginalName(relativeName);
                result.getRenamed().add(renamedVo);
            }
        }

        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.write(target, data);
        } catch (IOException e) {
            log.error("[raw-upload] write failed path={}", target, e);
            throw new BaseException("写入 raw 失败：" + e.getMessage());
        }

        log.info("[raw-upload] user={} path={} size={}",
                ShiroUtils.getUserId(), relativePath, data.length);

        RawUploadItemVo item = new RawUploadItemVo();
        item.setPath(relativePath);
        item.setSize((long) data.length);
        item.setOverwritten("OVERWRITE".equals(mode) && existed);
        result.getUploaded().add(item);
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BaseException("读取上传文件失败：" + e.getMessage());
        }
    }

    private static byte[] readZipEntry(ZipInputStream zis) throws IOException {
        byte[] buf = new byte[8192];
        int read;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        while ((read = zis.read(buf)) >= 0) {
            out.write(buf, 0, read);
        }
        return out.toByteArray();
    }

    private static String normalizeZipEntryName(String name) {
        if (name == null) {
            return "";
        }
        String n = name.trim().replace('\\', '/');
        while (n.startsWith("/")) {
            n = n.substring(1);
        }
        if (n.contains("..") || n.contains(":")) {
            throw new BaseException("非法 zip 路径: " + name);
        }
        return n;
    }

    private static String PathsLeaf(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
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

    private RenameResult resolveRenameTarget(Path rawRoot, String prefix, String relativeName) {
        String fileName = PathsLeaf(relativeName);
        String subDir = relativeName.contains("/")
                ? relativeName.substring(0, relativeName.lastIndexOf('/') + 1) : "";
        for (int n = 1; n < 10000; n++) {
            String candidateName = KbRawPathUtil.renameWithSuffix(fileName, n);
            String relativePath = prefix + "/" + subDir + candidateName;
            while (relativePath.contains("//")) {
                relativePath = relativePath.replace("//", "/");
            }
            Path target = KbRawPathUtil.normalizeUnder(rawRoot, relativePath);
            if (!Files.exists(target)) {
                RenameResult result = new RenameResult();
                result.relativePath = relativePath;
                result.target = target;
                return result;
            }
        }
        throw new BaseException("无法为文件生成唯一名称: " + relativeName);
    }

    private static final class RenameResult {
        private String relativePath;
        private Path target;
    }
}
