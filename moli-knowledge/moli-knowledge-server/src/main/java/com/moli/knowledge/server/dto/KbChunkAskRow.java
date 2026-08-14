package com.moli.knowledge.server.dto;

import lombok.Data;

/**
 * /kb/ask chunk 召回行：切段 + 父文档标题/摘要（JOIN kb_document）。
 */
@Data
public class KbChunkAskRow {

    private Long chunkId;
    private Long documentId;
    private Long spaceId;
    private String slug;
    private String title;
    private String summary;
    private String kbType;
    private String heading;
    private String content;
    private Integer chunkIndex;
}
