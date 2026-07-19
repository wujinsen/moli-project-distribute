package com.moli.ai.server.bi.support;

import com.moli.ai.server.bi.dto.BiColumnVo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BiQueryResult {

    private List<BiColumnVo> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();
    private int rowCount;
}
