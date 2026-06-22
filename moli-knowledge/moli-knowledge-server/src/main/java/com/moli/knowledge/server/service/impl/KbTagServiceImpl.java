package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbTag;
import com.moli.knowledge.server.mapper.KbTagMapper;
import com.moli.knowledge.server.service.KbTagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class KbTagServiceImpl implements KbTagService {

    @Resource
    private KbTagMapper kbTagMapper;

    @Override
    public List<KbTag> listBySpace(Long spaceId) {
        return kbTagMapper.selectList(new LambdaQueryWrapper<KbTag>()
                .eq(KbTag::getSpaceId, spaceId)
                .eq(KbTag::getIsDelete, CommonConstant.UN_DELETE)
                .orderByAsc(KbTag::getTagName));
    }

    @Override
    public Long create(KbTag tag) {
        if (tag.getSpaceId() == null || StringUtils.isBlank(tag.getTagName())) {
            throw new BaseException("空间ID和标签名不能为空");
        }
        tag.setId(IdGenerator.getId());
        kbTagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void update(KbTag tag) {
        KbTag existing = kbTagMapper.selectById(tag.getId());
        if (existing == null || !CommonConstant.UN_DELETE.equals(existing.getIsDelete())) {
            throw new BaseException("标签不存在");
        }
        kbTagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        KbTag tag = kbTagMapper.selectById(id);
        if (tag == null || !CommonConstant.UN_DELETE.equals(tag.getIsDelete())) {
            throw new BaseException("标签不存在");
        }
        tag.setIsDelete(CommonConstant.IS_DELETE);
        kbTagMapper.updateById(tag);
    }
}
