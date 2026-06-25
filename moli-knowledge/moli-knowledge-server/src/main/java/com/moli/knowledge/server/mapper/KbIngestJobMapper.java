package com.moli.knowledge.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.knowledge.server.entity.KbIngestJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KbIngestJobMapper extends BaseMapper<KbIngestJob> {
}
