package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.MinioProperties;
import com.moli.knowledge.server.entity.KbAttachment;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.mapper.KbAttachmentMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbAttachmentService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class KbAttachmentServiceImpl implements KbAttachmentService {

    private static final String OBJECT_PREFIX = "kb/attachment/";

    @Resource
    private KbAttachmentMapper kbAttachmentMapper;

    @Resource
    private KbDocumentMapper kbDocumentMapper;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;
    @Resource
    private KbAclService kbAclService;

    @Override
    public List<KbAttachment> listByDocument(Long documentId) {
        kbAclService.assertCanReadDocument(documentId);
        return kbAttachmentMapper.selectList(new LambdaQueryWrapper<KbAttachment>()
                .eq(KbAttachment::getDocumentId, documentId)
                .eq(KbAttachment::getIsDelete, CommonConstant.UN_DELETE)
                .orderByDesc(KbAttachment::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbAttachment upload(Long documentId, MultipartFile file) {
        if (documentId == null) {
            throw new BaseException("文档ID不能为空");
        }
        kbAclService.assertCanEditDocument(documentId);
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }
        KbDocument document = kbDocumentMapper.selectById(documentId);
        if (document == null || !CommonConstant.UN_DELETE.equals(document.getIsDelete())) {
            throw new BaseException("文档不存在");
        }

        Long attachmentId = IdGenerator.getId();
        String fileName = sanitizeFileName(file.getOriginalFilename());
        String objectKey = OBJECT_PREFIX + documentId + "/" + attachmentId + "/" + fileName;
        String contentType = StringUtils.defaultIfBlank(file.getContentType(), "application/octet-stream");

        try {
            PutObjectOptions options = new PutObjectOptions(file.getSize(), PutObjectOptions.MIN_MULTIPART_SIZE);
            options.setContentType(contentType);
            minioClient.putObject(
                    minioProperties.getBucket(),
                    objectKey,
                    file.getInputStream(),
                    options
            );
        } catch (Exception e) {
            log.error("MinIO upload failed, objectKey={}", objectKey, e);
            throw new BaseException("文件上传失败：" + e.getMessage());
        }

        KbAttachment attachment = new KbAttachment();
        attachment.setId(attachmentId);
        attachment.setDocumentId(documentId);
        attachment.setFileName(fileName);
        attachment.setObjectKey(objectKey);
        attachment.setFileSize(file.getSize());
        attachment.setContentType(contentType);
        attachment.setIsDelete(CommonConstant.UN_DELETE);
        kbAttachmentMapper.insert(attachment);
        return attachment;
    }

    @Override
    public void download(Long id, HttpServletResponse response) {
        KbAttachment attachment = getActiveAttachment(id);
        kbAclService.assertCanReadDocument(attachment.getDocumentId());
        try (InputStream inputStream = minioClient.getObject(minioProperties.getBucket(), attachment.getObjectKey())) {
            String encodedName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8.name())
                    .replace("+", "%20");
            response.setContentType(StringUtils.defaultIfBlank(attachment.getContentType(), "application/octet-stream"));
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            if (attachment.getFileSize() != null && attachment.getFileSize() > 0) {
                response.setContentLengthLong(attachment.getFileSize());
            }
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (MinioException e) {
            log.error("MinIO download failed, id={}, objectKey={}", id, attachment.getObjectKey(), e);
            throw new BaseException("文件下载失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("Attachment download failed, id={}", id, e);
            throw new BaseException("文件下载失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KbAttachment attachment = getActiveAttachment(id);
        kbAclService.assertCanEditDocument(attachment.getDocumentId());
        attachment.setIsDelete(CommonConstant.IS_DELETE);
        kbAttachmentMapper.updateById(attachment);
    }

    private KbAttachment getActiveAttachment(Long id) {
        if (id == null) {
            throw new BaseException("附件ID不能为空");
        }
        KbAttachment attachment = kbAttachmentMapper.selectById(id);
        if (attachment == null || !CommonConstant.UN_DELETE.equals(attachment.getIsDelete())) {
            throw new BaseException("附件不存在");
        }
        return attachment;
    }

    private String sanitizeFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "file";
        }
        String name = fileName.replace("\\", "/");
        int index = name.lastIndexOf('/');
        if (index >= 0) {
            name = name.substring(index + 1);
        }
        return StringUtils.isBlank(name) ? "file" : name;
    }
}
