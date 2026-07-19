package com.moli.knowledge.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.knowledge.server.dto.KbChunkAskRow;
import com.moli.knowledge.server.entity.KbDocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface KbDocumentChunkMapper extends BaseMapper<KbDocumentChunk> {

    /**
     * Query(/kb/ask) chunk 候选召回：ngram 全文按相关度 top-N 段，叠加空间/状态/类型过滤。
     */
    List<KbChunkAskRow> searchAskChunkCandidates(@Param("spaceIds") List<Long> spaceIds,
                                                 @Param("status") Integer status,
                                                 @Param("includeTypes") List<String> includeTypes,
                                                 @Param("excludeTypes") List<String> excludeTypes,
                                                 @Param("keyword") String keyword,
                                                 @Param("limit") int limit);

    /** 全文 0 命中时回退 LIKE（heading/content）。 */
    List<KbChunkAskRow> searchAskChunkCandidatesLike(@Param("spaceIds") List<Long> spaceIds,
                                                     @Param("status") Integer status,
                                                     @Param("includeTypes") List<String> includeTypes,
                                                     @Param("excludeTypes") List<String> excludeTypes,
                                                     @Param("keyword") String keyword,
                                                     @Param("limit") int limit);

    /** hybrid 融合后按 chunkId 补查切段正文（JOIN kb_document）。 */
    List<KbChunkAskRow> selectAskChunksByIds(@Param("chunkIds") List<Long> chunkIds,
                                             @Param("status") Integer status);

    /**
     * 图扩跳 Step 3：按 documentId 批量取 ask chunk，每 doc 至多 {@code perDoc} 段（优先 term 命中，否则 chunk_index 最小）。
     */
    List<KbChunkAskRow> selectAskChunksByDocumentIds(@Param("documentIds") Collection<Long> documentIds,
                                                     @Param("spaceIds") List<Long> spaceIds,
                                                     @Param("status") Integer status,
                                                     @Param("keyword") String keyword,
                                                     @Param("perDoc") int perDoc);
}
