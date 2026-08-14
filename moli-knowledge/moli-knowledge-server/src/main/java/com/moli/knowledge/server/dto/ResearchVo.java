package com.moli.knowledge.server.dto;

import com.moli.knowledge.server.guard.AskGuardVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("DeepResearch 结果（AI-10 additive）")
public class ResearchVo {

    private String runId;
    private String status;
    private String topic;
    private String title;
    private String slug;
    private Map<String, Object> outline;
    private String reportMd;
    private List<ResearchCitationVo> citations = new ArrayList<>();
    private List<Map<String, Object>> sectionEvidence = new ArrayList<>();
    private Double coverage;
    private List<String> unsupportedStatements = new ArrayList<>();
    private boolean degraded;
    private String degradeReason;
    private Long ingestJobId;
    private String outputPath;
    private AskGuardVo guard;
    private long latencyMs;

    @Data
    public static class ResearchCitationVo {
        private String slug;
        private String title;
        private List<String> sectionIds = new ArrayList<>();
    }
}
