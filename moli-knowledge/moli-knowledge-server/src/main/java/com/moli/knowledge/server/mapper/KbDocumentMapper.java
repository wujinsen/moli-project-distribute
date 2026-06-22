package com.moli.knowledge.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.entity.KbDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {

    /** MySQL ngram 全文检索（需 ftx_kb_document 索引）。 */
    Page<KbDocument> searchFullText(Page<KbDocument> page,
                                    @Param("spaceId") Long spaceId,
                                    @Param("spaceIds") List<Long> spaceIds,
                                    @Param("categoryId") Long categoryId,
                                    @Param("status") Integer status,
                                    @Param("documentIds") List<Long> documentIds,
                                    @Param("keyword") String keyword);
}
