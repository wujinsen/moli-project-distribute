package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.CategoryTreeVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KbCategoryServiceImpl implements KbCategoryService {

    @Resource
    private KbCategoryMapper kbCategoryMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public List<CategoryTreeVo> tree(Long spaceId) {
        kbAclService.assertCanRead(spaceId);
        List<KbCategory> categories = kbCategoryMapper.selectList(new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getSpaceId, spaceId)
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE)
                .orderByAsc(KbCategory::getSort)
                .orderByAsc(KbCategory::getId));
        Map<Long, List<KbCategory>> grouped = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        return buildTree(grouped, 0L);
    }

    private List<CategoryTreeVo> buildTree(Map<Long, List<KbCategory>> grouped, Long parentId) {
        List<KbCategory> children = grouped.getOrDefault(parentId, new ArrayList<>());
        List<CategoryTreeVo> result = new ArrayList<>();
        for (KbCategory category : children) {
            CategoryTreeVo vo = new CategoryTreeVo();
            BeanUtils.copyProperties(category, vo);
            vo.setChildren(buildTree(grouped, category.getId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public Long create(KbCategory category) {
        if (category.getSpaceId() == null) {
            throw new BaseException("空间ID不能为空");
        }
        kbAclService.assertCanEdit(category.getSpaceId());
        if (StringUtils.isBlank(category.getCategoryName())) {
            throw new BaseException("分类名称不能为空");
        }
        category.setId(IdGenerator.getId());
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        kbCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void update(KbCategory category) {
        KbCategory existing = kbCategoryMapper.selectById(category.getId());
        if (existing == null || !CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {
            throw new BaseException("分类不存在");
        }
        kbAclService.assertCanEdit(existing.getSpaceId());
        kbCategoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        KbCategory category = kbCategoryMapper.selectById(id);
        if (category == null || !CommonConstant.UN_DELETE.equals(category.getIsDelete())) {
            throw new BaseException("分类不存在");
        }
        kbAclService.assertCanEdit(category.getSpaceId());
        Integer childCount = kbCategoryMapper.selectCount(new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getParentId, id)
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE));
        if (childCount != null && childCount > 0) {
            throw new BaseException("请先删除子分类");
        }
        category.setIsDelete(CommonConstant.IS_DELETE);
        kbCategoryMapper.updateById(category);
    }
}
