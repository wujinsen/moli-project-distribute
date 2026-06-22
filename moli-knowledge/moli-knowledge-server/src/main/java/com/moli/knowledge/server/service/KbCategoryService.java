package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.CategoryTreeVo;
import com.moli.knowledge.server.entity.KbCategory;

import java.util.List;

public interface KbCategoryService {

    List<CategoryTreeVo> tree(Long spaceId);

    Long create(KbCategory category);

    void update(KbCategory category);

    void delete(Long id);
}
