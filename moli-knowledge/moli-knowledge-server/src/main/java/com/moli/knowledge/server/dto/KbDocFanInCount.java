package com.moli.knowledge.server.dto;

import lombok.Data;

/** kb_relation 入度聚合行（AI-5 hub 惩罚）。 */
@Data
public class KbDocFanInCount {
    private Long targetDocId;
    private Integer cnt;
}
