package com.moli.ai.server.bi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BiChartVo {

    private String type = "table";
    private String x;
    private List<String> y = new ArrayList<>();
    private String title;
}
