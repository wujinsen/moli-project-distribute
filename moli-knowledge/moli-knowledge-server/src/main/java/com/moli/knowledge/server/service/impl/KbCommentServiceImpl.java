package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbComment;
import com.moli.knowledge.server.mapper.KbCommentMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbCommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class KbCommentServiceImpl implements KbCommentService {

    @Resource
    private KbCommentMapper kbCommentMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public Page<KbComment> page(Long documentId, int pageNum, int pageSize) {
        kbAclService.assertCanReadDocument(documentId);
        return kbCommentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<KbComment>()
                        .eq(KbComment::getDocumentId, documentId)
                        .eq(KbComment::getIsDelete, CommonConstant.UN_DELETE)
                        .orderByDesc(KbComment::getCreateTime));
    }

    @Override
    public Long create(KbComment comment) {
        if (comment.getDocumentId() == null || StringUtils.isBlank(comment.getContent())) {
            throw new BaseException("文档ID和评论内容不能为空");
        }
        kbAclService.assertCanReadDocument(comment.getDocumentId());
        comment.setId(IdGenerator.getId());
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        kbCommentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public void delete(Long id) {
        KbComment comment = kbCommentMapper.selectById(id);
        if (comment == null || !CommonConstant.UN_DELETE.equals(comment.getIsDelete())) {
            throw new BaseException("评论不存在");
        }
        kbAclService.assertCanEditDocument(comment.getDocumentId());
        comment.setIsDelete(CommonConstant.IS_DELETE);
        kbCommentMapper.updateById(comment);
    }
}
