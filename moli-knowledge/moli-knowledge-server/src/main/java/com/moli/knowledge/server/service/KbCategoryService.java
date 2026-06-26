package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.CategoryTreeVo;
import com.moli.knowledge.server.entity.KbCategory;

import java.util.List;

public interface KbCategoryService {

    /** 分类树；withCount=true 时附带每个分类下的文档数。 */
    List<CategoryTreeVo> tree(Long spaceId, boolean withCount);

    /** 创建分类：写库 + 在 wiki 目录下创建对应子目录（dir_slug）。 */
    Long create(KbCategory category);

    /** 更新分类：仅改显示名/图标/排序/默认体裁；dir_slug 不可变。 */
    void update(KbCategory category);

    /** 删除分类：要求绑定目录为空（无 .md）且无文档归属；删目录 + 软删。 */
    void delete(Long id);
}
