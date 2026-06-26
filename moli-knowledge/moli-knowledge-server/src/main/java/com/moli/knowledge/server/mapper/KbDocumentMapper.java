package com.moli.knowledge.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.entity.KbDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.moli.knowledge.server.dto.KbTypeCountRow;

@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {

    /** MySQL ngram 全文检索（需 ftx_kb_document 索引）。 */
    Page<KbDocument> searchFullText(Page<KbDocument> page,
                                    @Param("spaceId") Long spaceId,
                                    @Param("spaceIds") List<Long> spaceIds,
                                    @Param("categoryId") Long categoryId,
                                    @Param("status") Integer status,
                                    @Param("documentIds") List<Long> documentIds,
                                    @Param("keyword") String keyword,
                                    @Param("source") String source);

    /**
     * Query(/kb/ask) 候选召回：ngram 全文按相关度召回 top-N，叠加空间/状态/类型作用域过滤。
     * 用于替代「全量 selectList + 内存打分」，把候选集从全库收敛到 limit 条再做精排。
     */
    List<KbDocument> searchAskCandidates(@Param("spaceIds") List<Long> spaceIds,
                                         @Param("status") Integer status,
                                         @Param("includeTypes") List<String> includeTypes,
                                         @Param("excludeTypes") List<String> excludeTypes,
                                         @Param("keyword") String keyword,
                                         @Param("limit") int limit);

    /** 浏览目录 meta：按 kb_type 统计已发布文档数 */
    List<KbTypeCountRow> countPublishedByKbType(@Param("spaceId") Long spaceId,
                                                @Param("spaceIds") List<Long> spaceIds,
                                                @Param("status") Integer status);
}
