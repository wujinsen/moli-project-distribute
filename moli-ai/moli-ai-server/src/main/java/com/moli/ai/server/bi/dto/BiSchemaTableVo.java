package com.moli.ai.server.bi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BiSchemaTableVo {

    private String table;
    private String comment;
    private List<BiSchemaColumnVo> columns = new ArrayList<>();
}
