package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.CategoryTreeVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbCategoryService;
import com.moli.knowledge.server.support.KbCategoryConstants;
import com.moli.knowledge.server.support.KbPublishedWikiFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class KbCategoryServiceImpl implements KbCategoryService {

    /** dir_slug 合法性：单段英文/数字/连字符/下划线，禁止斜杠与点（防越权 + 与 wiki 目录约定一致）。 */
    private static final Pattern DIR_SLUG = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    @Resource
    private KbCategoryMapper kbCategoryMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbAclService kbAclService;

    @Override
    public List<CategoryTreeVo> tree(Long spaceId, boolean withCount) {
        kbAclService.assertCanRead(spaceId);
        List<KbCategory> categories = kbCategoryMapper.selectList(new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getSpaceId, spaceId)
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE)
                .orderByAsc(KbCategory::getSort)
                .orderByAsc(KbCategory::getId));
        Map<Long, Integer> countMap = withCount ? countByCategory(spaceId) : null;
        Map<Long, List<KbCategory>> grouped = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        List<CategoryTreeVo> result = buildTree(grouped, 0L, countMap);
        if (withCount && countMap != null) {
            int uncatCount = countMap.getOrDefault(-1L, 0);
            if (uncatCount > 0) {
                CategoryTreeVo virtual = new CategoryTreeVo();
                virtual.setVirtualNode(true);
                virtual.setCategoryName(KbCategoryConstants.UNCATEGORIZED_LABEL);
                virtual.setDirSlug(KbCategoryConstants.UNCATEGORIZED_DIR_SLUG);
                virtual.setDocCount(uncatCount);
                virtual.setSort(9999);
                virtual.setSpaceId(spaceId);
                result.add(virtual);
            }
        }
        return result;
    }

    /** 统计各分类下已发布 wiki 文档数（与浏览 /kb/index 一致：source=kb + status=published）。 */
    private Map<Long, Integer> countByCategory(Long spaceId) {
        QueryWrapper<KbDocument> qw = KbPublishedWikiFilter.publishedKbQuery(spaceId);
        qw.select("category_id AS category_id", "count(*) AS cnt");
        qw.groupBy("category_id");
        Map<Long, Integer> map = new HashMap<>();
        for (Map<String, Object> row : kbDocumentMapper.selectMaps(qw)) {
            Object cid = row.get("category_id");
            Object cnt = row.get("cnt");
            Long key = cid == null ? -1L : Long.valueOf(cid.toString());
            map.put(key, cnt == null ? 0 : Integer.valueOf(cnt.toString()));
        }
        return map;
    }

    private List<CategoryTreeVo> buildTree(Map<Long, List<KbCategory>> grouped, Long parentId,
                                           Map<Long, Integer> countMap) {
        List<KbCategory> children = grouped.getOrDefault(parentId, new ArrayList<>());
        List<CategoryTreeVo> result = new ArrayList<>();
        for (KbCategory category : children) {
            CategoryTreeVo vo = new CategoryTreeVo();
            BeanUtils.copyProperties(category, vo);
            if (countMap != null) {
                vo.setDocCount(countMap.getOrDefault(category.getId(), 0));
            }
            vo.setChildren(buildTree(grouped, category.getId(), countMap));
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
        String dirSlug = StringUtils.trimToNull(category.getDirSlug());
        if (dirSlug == null || !DIR_SLUG.matcher(dirSlug).matches()) {
            throw new BaseException("目录名(dir_slug)非法：仅允许英文/数字/连字符/下划线，单段");
        }
        // 同空间 dir_slug 唯一
        Integer dup = kbCategoryMapper.selectCount(new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getSpaceId, category.getSpaceId())
                .eq(KbCategory::getDirSlug, dirSlug)
                .eq(KbCategory::getIsDelete, CommonConstant.UN_DELETE));
        if (dup != null && dup > 0) {
            throw new BaseException("目录已存在: " + dirSlug);
        }

        KbSpace space = loadSpace(category.getSpaceId());
        Path dir = resolveCategoryDir(space.getSpaceCode(), dirSlug);
        try {
            Files.createDirectories(dir);
            Path keep = dir.resolve(".gitkeep");
            if (!Files.exists(keep)) {
                Files.write(keep, new byte[0]);
            }
        } catch (IOException e) {
            log.error("创建分类目录失败: {}", dir, e);
            throw new BaseException("创建分类目录失败：" + e.getMessage());
        }

        category.setId(IdGenerator.getId());
        category.setDirSlug(dirSlug);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        category.setIsDelete(CommonConstant.UN_DELETE);
        kbCategoryMapper.insert(category);
        log.info("[category] create space={} dir={} name={}",
                space.getSpaceCode(), dirSlug, category.getCategoryName());
        return category.getId();
    }

    @Override
    public void update(KbCategory category) {
        KbCategory existing = kbCategoryMapper.selectById(category.getId());
        if (existing == null || !CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {
            throw new BaseException("分类不存在");
        }
        kbAclService.assertCanEdit(existing.getSpaceId());
        // 仅允许改显示名/图标/排序；dir_slug、space_id、parent_id 不可经此变更
        KbCategory patch = new KbCategory();
        patch.setId(existing.getId());
        patch.setCategoryName(StringUtils.trimToNull(category.getCategoryName()));
        patch.setIcon(category.getIcon());
        patch.setSort(category.getSort());
        kbCategoryMapper.updateById(patch);
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
        Integer docCount = kbDocumentMapper.selectCount(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getCategoryId, id)
                .eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE));
        if (docCount != null && docCount > 0) {
            throw new BaseException("分类下还有 " + docCount + " 篇文档，请先移走再删除");
        }

        // 物理目录必须为空（仅允许残留 .gitkeep）
        if (StringUtils.isNotBlank(category.getDirSlug())) {
            KbSpace space = loadSpace(category.getSpaceId());
            Path dir = resolveCategoryDir(space.getSpaceCode(), category.getDirSlug());
            removeEmptyDir(dir);
        }

        category.setIsDelete(CommonConstant.IS_DELETE);
        kbCategoryMapper.updateById(category);
        log.info("[category] delete id={} dir={}", id, category.getDirSlug());
    }

    private void removeEmptyDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> s = Files.list(dir)) {
            List<Path> entries = s.collect(Collectors.toList());
            for (Path p : entries) {
                if (p.getFileName().toString().endsWith(".md")) {
                    throw new BaseException("目录非空（含 .md），不能删除: " + dir.getFileName());
                }
            }
            // 仅剩 .gitkeep 等：清理后删目录
            for (Path p : entries) {
                Files.deleteIfExists(p);
            }
            Files.deleteIfExists(dir);
        } catch (IOException e) {
            throw new BaseException("删除分类目录失败：" + e.getMessage());
        }
    }

    private KbSpace loadSpace(Long spaceId) {
        KbSpace space = kbSpaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BaseException("空间不存在");
        }
        return space;
    }

    /** wiki 根 / {空间目录} / {dir_slug}，并校验不越权。 */
    private Path resolveCategoryDir(String spaceCode, String dirSlug) {
        String wikiDir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(wikiDir)) {
            throw new BaseException("空间未配置 wiki 目录: " + spaceCode);
        }
        Path root = com.moli.knowledge.server.util.KbRepoPathUtil.resolveKbRoot(wikiProperties.getRoot());
        Path base = root.resolve(wikiDir).normalize();
        Path dir = base.resolve(dirSlug).normalize();
        if (!dir.startsWith(base)) {
            throw new BaseException("非法目录（越权）: " + dirSlug);
        }
        return dir;
    }
}
