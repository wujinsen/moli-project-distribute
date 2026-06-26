package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbSearchProperties;
import com.moli.knowledge.server.dto.DocumentDetailVo;
import com.moli.knowledge.server.dto.DocumentSaveRequest;
import com.moli.knowledge.server.dto.DocumentSearchRequest;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentTag;
import com.moli.knowledge.server.entity.KbDocumentVersion;
import com.moli.knowledge.server.entity.KbFavorite;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbDocumentTagMapper;
import com.moli.knowledge.server.mapper.KbDocumentVersionMapper;
import com.moli.knowledge.server.mapper.KbFavoriteMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbDocumentService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KbDocumentServiceImpl implements KbDocumentService {

    private static final Logger log = LoggerFactory.getLogger(KbDocumentServiceImpl.class);

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentTagMapper kbDocumentTagMapper;
    @Resource
    private KbDocumentVersionMapper kbDocumentVersionMapper;
    @Resource
    private KbFavoriteMapper kbFavoriteMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbSearchProperties kbSearchProperties;

    @Override
    public Page<KbDocument> search(DocumentSearchRequest request) {
        List<Long> scopeSpaces = kbAclService.resolveReadableSpaceIds(
                request.getSpaceId(), request.getSpaceIds());
        if (scopeSpaces.isEmpty()) {
            return new Page<>(request.getPageNum(), request.getPageSize(), 0);
        }
        Long singleSpaceId = scopeSpaces.size() == 1 ? scopeSpaces.get(0) : null;
        List<Long> multiSpaceIds = scopeSpaces.size() == 1 ? null : scopeSpaces;

        List<Long> documentIds = null;
        if (request.getTagId() != null) {
            List<KbDocumentTag> relations = kbDocumentTagMapper.selectList(new LambdaQueryWrapper<KbDocumentTag>()
                    .eq(KbDocumentTag::getTagId, request.getTagId()));
            if (CollectionUtils.isEmpty(relations)) {
                return new Page<>(request.getPageNum(), request.getPageSize(), 0);
            }
            documentIds = relations.stream().map(KbDocumentTag::getDocumentId).collect(Collectors.toList());
        }

        if (StringUtils.isNotBlank(request.getKeyword()) && kbSearchProperties.fullTextEnabled()) {
            try {
                return kbDocumentMapper.searchFullText(
                        new Page<>(request.getPageNum(), request.getPageSize()),
                        singleSpaceId,
                        multiSpaceIds,
                        request.getCategoryId(),
                        request.getStatus(),
                        documentIds,
                        request.getKeyword().trim(),
                        StringUtils.trimToNull(request.getSource()));
            } catch (Exception e) {
                log.warn("Fulltext search failed, fallback to LIKE: {}", e.getMessage());
            }
        }

        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        if (singleSpaceId != null) {
            wrapper.eq(KbDocument::getSpaceId, singleSpaceId);
        } else {
            wrapper.in(KbDocument::getSpaceId, scopeSpaces);
        }
        if (request.getCategoryId() != null) {
            wrapper.eq(KbDocument::getCategoryId, request.getCategoryId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(KbDocument::getStatus, request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(KbDocument::getTitle, request.getKeyword())
                    .or().like(KbDocument::getSummary, request.getKeyword())
                    .or().like(KbDocument::getContent, request.getKeyword()));
        }
        if (request.getTagId() != null) {
            wrapper.in(KbDocument::getId, documentIds);
        }
        if (StringUtils.isNotBlank(request.getSource())) {
            wrapper.eq(KbDocument::getSource, request.getSource().trim());
        }
        wrapper.orderByDesc(KbDocument::getUpdateTime);
        return kbDocumentMapper.selectPage(new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
    }

    @Override
    public DocumentDetailVo detail(Long id) {
        KbDocument document = getActiveDocument(id);
        kbAclService.assertCanRead(document.getSpaceId());
        document.setViewCount(document.getViewCount() == null ? 1 : document.getViewCount() + 1);
        kbDocumentMapper.updateById(document);

        DocumentDetailVo vo = new DocumentDetailVo();
        BeanUtils.copyProperties(document, vo);
        vo.setTagIds(listTagIds(id));
        vo.setFavorited(isFavorited(id));
        return vo;
    }

    @Override
    public Long save(DocumentSaveRequest request) {
        rejectDirectDocumentWrite();
        return null; // unreachable
    }

    private void rejectDirectDocumentWrite() {
        throw new BaseException(
                "已停用 Web 直连写库：请通过 PUT /kb/wiki/page 保存 wiki 源文件，再触发 Wiki 同步（sync_to_db）");
    }

    @Override
    public void publish(Long id) {
        rejectDirectDocumentWrite();
    }

    @Override
    public void archive(Long id) {
        rejectDirectDocumentWrite();
    }

    @Override
    public void delete(Long id) {
        rejectDirectDocumentWrite();
    }

    @Override
    public Page<KbDocumentVersion> versions(Long documentId, int pageNum, int pageSize) {
        KbDocument document = getActiveDocument(documentId);
        kbAclService.assertCanRead(document.getSpaceId());
        return kbDocumentVersionMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<KbDocumentVersion>()
                        .eq(KbDocumentVersion::getDocumentId, documentId)
                        .orderByDesc(KbDocumentVersion::getVersionNo));
    }

    private KbDocument getActiveDocument(Long id) {
        KbDocument document = kbDocumentMapper.selectById(id);
        if (document == null || !CommonConstant.UN_DELETE.equals(document.getIsDelete())) {
            throw new BaseException("文档不存在");
        }
        return document;
    }

    private List<Long> listTagIds(Long documentId) {
        return kbDocumentTagMapper.selectList(new LambdaQueryWrapper<KbDocumentTag>()
                        .eq(KbDocumentTag::getDocumentId, documentId))
                .stream().map(KbDocumentTag::getTagId).collect(Collectors.toList());
    }

    private boolean isFavorited(Long documentId) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return false;
        }
        Integer count = kbFavoriteMapper.selectCount(new LambdaQueryWrapper<KbFavorite>()
                .eq(KbFavorite::getUserId, userId)
                .eq(KbFavorite::getDocumentId, documentId));
        return count != null && count > 0;
    }
}
