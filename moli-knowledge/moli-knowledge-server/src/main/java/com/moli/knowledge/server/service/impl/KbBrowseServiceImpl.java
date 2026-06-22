package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.PageDetailVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentTag;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.entity.KbTag;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbDocumentTagMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbTagMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbBrowseService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KbBrowseServiceImpl implements KbBrowseService {

    /** 类型展示顺序与中文名。 */
    private static final String[][] TYPE_LABELS = {
            {"guide", "操作指导"}, {"service", "微服务"}, {"concept", "概念"},
            {"article", "技术文章"}, {"interview", "面试题"}, {"output", "综合"},
    };

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentTagMapper kbDocumentTagMapper;
    @Resource
    private KbTagMapper kbTagMapper;
    @Resource
    private KbRelationMapper kbRelationMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public IndexTreeVo index(Long spaceId) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        wrapper.eq(KbDocument::getStatus, DocumentStatus.PUBLISHED.getCode());
        // ACL：指定空间校验可读；否则限定到可读空间集合
        if (spaceId != null) {
            kbAclService.assertCanRead(spaceId);
            wrapper.eq(KbDocument::getSpaceId, spaceId);
        } else {
            List<Long> accessible = kbAclService.accessibleSpaceIds();
            if (accessible.isEmpty()) {
                return new IndexTreeVo();
            }
            wrapper.in(KbDocument::getSpaceId, accessible);
        }
        wrapper.orderByAsc(KbDocument::getTitle);
        List<KbDocument> docs = kbDocumentMapper.selectList(wrapper);

        // type -> Group（按预设顺序）
        Map<String, IndexTreeVo.Group> groups = new LinkedHashMap<>();
        for (String[] tl : TYPE_LABELS) {
            groups.put(tl[0], new IndexTreeVo.Group(tl[0], tl[1]));
        }
        IndexTreeVo.Group other = new IndexTreeVo.Group("other", "其它");

        for (KbDocument d : docs) {
            IndexTreeVo.Group g = d.getKbType() == null ? other
                    : groups.getOrDefault(d.getKbType(), other);
            g.getItems().add(new IndexTreeVo.Item(d.getId(), d.getSlug(), d.getTitle(), d.getSummary(), d.getSpaceId()));
        }

        IndexTreeVo vo = new IndexTreeVo();
        vo.setTotal(docs.size());
        for (IndexTreeVo.Group g : groups.values()) {
            if (!g.getItems().isEmpty()) {
                vo.getGroups().add(g);
            }
        }
        if (!other.getItems().isEmpty()) {
            vo.getGroups().add(other);
        }
        return vo;
    }

    @Override
    public PageDetailVo page(String slug, Long spaceId) {
        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        wrapper.eq(KbDocument::getSlug, slug);
        if (spaceId != null) {
            wrapper.eq(KbDocument::getSpaceId, spaceId);
        }
        wrapper.last("limit 1");
        KbDocument d = kbDocumentMapper.selectOne(wrapper);
        if (d == null) {
            throw new BaseException("页面不存在: " + slug);
        }
        kbAclService.assertCanRead(d.getSpaceId());
        d.setViewCount(d.getViewCount() == null ? 1 : d.getViewCount() + 1);
        kbDocumentMapper.updateById(d);

        PageDetailVo vo = new PageDetailVo();
        vo.setDocId(d.getId());
        vo.setSpaceId(d.getSpaceId());
        vo.setSlug(d.getSlug());
        vo.setTitle(d.getTitle());
        vo.setSummary(d.getSummary());
        vo.setContent(d.getContent());
        vo.setKbType(d.getKbType());
        vo.setDomain(d.getDomain());
        vo.setStatus(d.getStatus());
        vo.setUpdateTime(d.getUpdateTime());
        vo.setTags(tagNames(d.getId()));
        vo.setOutLinks(links(d.getId(), true));
        vo.setBackLinks(links(d.getId(), false));
        return vo;
    }

    private List<String> tagNames(Long docId) {
        List<KbDocumentTag> rels = kbDocumentTagMapper.selectList(
                new LambdaQueryWrapper<KbDocumentTag>().eq(KbDocumentTag::getDocumentId, docId));
        if (CollectionUtils.isEmpty(rels)) {
            return new ArrayList<>();
        }
        List<Long> tagIds = rels.stream().map(KbDocumentTag::getTagId).collect(Collectors.toList());
        return kbTagMapper.selectBatchIds(tagIds).stream()
                .map(KbTag::getTagName).collect(Collectors.toList());
    }

    /** out=true 取出链（source=docId），否则取入链（target=docId）。仅 resolved=1。 */
    private List<PageDetailVo.Ref> links(Long docId, boolean out) {
        LambdaQueryWrapper<KbRelation> w = new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 1);
        if (out) {
            w.eq(KbRelation::getSourceDocId, docId);
        } else {
            w.eq(KbRelation::getTargetDocId, docId);
        }
        List<KbRelation> rels = kbRelationMapper.selectList(w);
        List<PageDetailVo.Ref> refs = new ArrayList<>();
        for (KbRelation r : rels) {
            Long otherId = out ? r.getTargetDocId() : r.getSourceDocId();
            if (otherId == null) {
                continue;
            }
            KbDocument od = kbDocumentMapper.selectById(otherId);
            if (od != null && CommonConstant.UN_DELETE.equals(od.getIsDelete())) {
                refs.add(new PageDetailVo.Ref(od.getId(), od.getSlug(), od.getTitle(), r.getRelationType()));
            }
        }
        return refs;
    }
}
