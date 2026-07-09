package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationStatsVo {

    private int projects;
    private int servers;
    private int platforms;
    private int components;
    private int portMismatches;
    private int healthDown;

    private List<OperationEnvCountVo> envBreakdown = new ArrayList<>();
}
