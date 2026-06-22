package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbFavorite;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbFavoriteMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbFavoriteService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KbFavoriteServiceImpl implements KbFavoriteService {

    @Resource
    private KbFavoriteMapper kbFavoriteMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public void add(Long documentId) {
        Long userId = requireUserId();
        kbAclService.assertCanReadDocument(documentId);
        Integer count = kbFavoriteMapper.selectCount(new LambdaQueryWrapper<KbFavorite>()
                .eq(KbFavorite::getUserId, userId)
                .eq(KbFavorite::getDocumentId, documentId));
        if (count != null && count > 0) {
            return;
        }
        KbFavorite favorite = new KbFavorite();
        favorite.setId(IdGenerator.getId());
        favorite.setUserId(userId);
        favorite.setDocumentId(documentId);
        favorite.setCreateTime(new Date());
        kbFavoriteMapper.insert(favorite);
    }

    @Override
    public void remove(Long documentId) {
        Long userId = requireUserId();
        kbFavoriteMapper.delete(new LambdaQueryWrapper<KbFavorite>()
                .eq(KbFavorite::getUserId, userId)
                .eq(KbFavorite::getDocumentId, documentId));
    }

    @Override
    public Page<KbDocument> myFavorites(int pageNum, int pageSize) {
        Long userId = requireUserId();
        Page<KbFavorite> favoritePage = kbFavoriteMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<KbFavorite>()
                        .eq(KbFavorite::getUserId, userId)
                        .orderByDesc(KbFavorite::getCreateTime));
        Page<KbDocument> result = new Page<>(pageNum, pageSize, favoritePage.getTotal());
        if (favoritePage.getRecords().isEmpty()) {
            result.setRecords(new ArrayList<>());
            return result;
        }
        List<Long> docIds = favoritePage.getRecords().stream()
                .map(KbFavorite::getDocumentId).collect(Collectors.toList());
        List<Long> accessible = kbAclService.accessibleSpaceIds();
        if (accessible.isEmpty()) {
            result.setRecords(new ArrayList<>());
            return result;
        }
        List<KbDocument> documents = kbDocumentMapper.selectList(new LambdaQueryWrapper<KbDocument>()
                .in(KbDocument::getId, docIds)
                .eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE)
                .in(KbDocument::getSpaceId, kbAclService.accessibleSpaceIds()));
        result.setRecords(documents);
        return result;
    }

    private Long requireUserId() {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        return userId;
    }
}
