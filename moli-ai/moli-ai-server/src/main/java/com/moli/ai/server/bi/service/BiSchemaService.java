package com.moli.ai.server.bi.service;

import com.moli.ai.server.bi.dto.BiSchemaTableVo;

import java.util.List;

public interface BiSchemaService {

    List<BiSchemaTableVo> listAllowedSchema();
}
