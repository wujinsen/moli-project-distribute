package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.DocumentDetailVo;
import com.moli.knowledge.server.dto.DocumentSaveRequest;
import com.moli.knowledge.server.dto.DocumentSearchRequest;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentTag;
import com.moli.knowledge.server.entity.KbDocumentVersion;
import com.moli.knowledge.server.entity.KbFavorite;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbDocumentTagMapper;
import com.moli.knowledge.server.mapper.KbDocumentVersionMapper;
import com.moli.knowledge.server.mapper.KbFavoriteMapper;
import com.moli.knowledge.server.service.KbDocumentService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KbDocumentServiceImpl implements KbDocumentService {

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentTagMapper kbDocumentTagMapper;
    @Resource
    private KbDocumentVersionMapper kbDocumentVersionMapper;
    @Resource
    private KbFavoriteMapper kbFavoriteMapper;

    @Override
    public Page<KbDocument> search(DocumentSearchRequest request) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        if (request.getSpaceId() != null) {
            wrapper.eq(KbDocument::getSpaceId, request.getSpaceId());
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
            List<KbDocumentTag> relations = kbDocumentTagMapper.selectList(new LambdaQueryWrapper<KbDocumentTag>()
                    .eq(KbDocumentTag::getTagId, request.getTagId()));
            if (CollectionUtils.isEmpty(relations)) {
                return new Page<>(request.getPageNum(), request.getPageSize(), 0);
            }
            wrapper.in(KbDocument::getId, relations.stream().map(KbDocumentTag::getDocumentId).collect(Collectors.toList()));
        }
        wrapper.orderByDesc(KbDocument::getUpdateTime);
        return kbDocumentMapper.selectPage(new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
    }

    @Override
    public DocumentDetailVo detail(Long id) {
        KbDocument document = getActiveDocument(id);
        document.setViewCount(document.getViewCount() == null ? 1 : document.getViewCount() + 1);
        kbDocumentMapper.updateById(document);

        DocumentDetailVo vo = new DocumentDetailVo();
        BeanUtils.copyProperties(document, vo);
        vo.setTagIds(listTagIds(id));
        vo.setFavorited(isFavorited(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(DocumentSaveRequest request) {
        if (request.getId() == null) {
            return createDocument(request);
        }
        return updateDocument(request);
    }

    private Long createDocument(DocumentSaveRequest request) {
        KbDocument document = new KbDocument();
        BeanUtils.copyProperties(request, document);
        document.setId(IdGenerator.getId());
        document.setDocType(StringUtils.defaultIfBlank(request.getDocType(), "markdown"));
        document.setStatus(request.getStatus() == null ? DocumentStatus.DRAFT.getCode() : request.getStatus());
        document.setViewCount(0);
        document.setLikeCount(0);
        document.setVersionNo(1);
        if (DocumentStatus.PUBLISHED.getCode() == document.getStatus()) {
            document.setPublishTime(new Date());
        }
        kbDocumentMapper.insert(document);
        saveDocumentTags(document.getId(), request.getTagIds());
        saveVersion(document, request.getChangeLog());
        return document.getId();
    }

    private Long updateDocument(DocumentSaveRequest request) {
        KbDocument existing = getActiveDocument(request.getId());
        int nextVersion = existing.getVersionNo() == null ? 1 : existing.getVersionNo() + 1;

        KbDocument document = new KbDocument();
        BeanUtils.copyProperties(request, document);
        document.setVersionNo(nextVersion);
        if (DocumentStatus.PUBLISHED.getCode() == document.getStatus() && existing.getPublishTime() == null) {
            document.setPublishTime(new Date());
        }
        kbDocumentMapper.updateById(document);
        kbDocumentTagMapper.delete(new LambdaQueryWrapper<KbDocumentTag>().eq(KbDocumentTag::getDocumentId, document.getId()));
        saveDocumentTags(document.getId(), request.getTagIds());
        saveVersion(document, request.getChangeLog());
        return document.getId();
    }

    @Override
    public void publish(Long id) {
        KbDocument document = getActiveDocument(id);
        document.setStatus(DocumentStatus.PUBLISHED.getCode());
        if (document.getPublishTime() == null) {
            document.setPublishTime(new Date());
        }
        kbDocumentMapper.updateById(document);
    }

    @Override
    public void archive(Long id) {
        KbDocument document = getActiveDocument(id);
        document.setStatus(DocumentStatus.ARCHIVED.getCode());
        kbDocumentMapper.updateById(document);
    }

    @Override
    public void delete(Long id) {
        KbDocument document = getActiveDocument(id);
        document.setIsDelete(CommonConstant.IS_DELETE);
        kbDocumentMapper.updateById(document);
    }

    @Override
    public Page<KbDocumentVersion> versions(Long documentId, int pageNum, int pageSize) {
        getActiveDocument(documentId);
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

    private void saveDocumentTags(Long documentId, List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        for (Long tagId : tagIds) {
            KbDocumentTag relation = new KbDocumentTag();
            relation.setId(IdGenerator.getId());
            relation.setDocumentId(documentId);
            relation.setTagId(tagId);
            kbDocumentTagMapper.insert(relation);
        }
    }

    private void saveVersion(KbDocument document, String changeLog) {
        KbDocumentVersion version = new KbDocumentVersion();
        version.setId(IdGenerator.getId());
        version.setDocumentId(document.getId());
        version.setVersionNo(document.getVersionNo());
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setChangeLog(changeLog);
        version.setCreateId(ShiroUtils.getUserId());
        version.setCreateTime(new Date());
        kbDocumentVersionMapper.insert(version);
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
