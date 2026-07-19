package com.moli.knowledge.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.knowledge.server.dto.KbDocFanInCount;
import com.moli.knowledge.server.entity.KbRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface KbRelationMapper extends BaseMapper<KbRelation> {

    /**
     * 出边：source_doc_id ∈ srcIds，resolved=1、is_delete=0，ACL 空间过滤。
     */
    List<KbRelation> selectBySourceDocIds(@Param("sourceDocIds") Collection<Long> sourceDocIds,
                                          @Param("spaceIds") List<Long> spaceIds);

    /**
     * supersedes 入边：target_doc_id ∈ targetDocIds（旧页入口→带出更新 source 页）。
     */
    List<KbRelation> selectSupersedesByTargetDocIds(@Param("targetDocIds") Collection<Long> targetDocIds,
                                                    @Param("spaceIds") List<Long> spaceIds);

    /**
     * 其它类型入边（graph.inbound=true 时）：不含 supersedes。
     */
    List<KbRelation> selectInboundByTargetDocIds(@Param("targetDocIds") Collection<Long> targetDocIds,
                                                 @Param("spaceIds") List<Long> spaceIds);

    /**
     * 入度统计（hub 惩罚）：target_doc_id ∈ ids，resolved=1、is_delete=0。
     */
    List<KbDocFanInCount> countInboundByTargetDocIds(@Param("targetDocIds") Collection<Long> targetDocIds,
                                                     @Param("spaceIds") List<Long> spaceIds);
}
