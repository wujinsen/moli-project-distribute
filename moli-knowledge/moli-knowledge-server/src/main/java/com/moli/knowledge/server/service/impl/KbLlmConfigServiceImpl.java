package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.dto.KbLlmConfigVo;
import com.moli.knowledge.server.service.KbLlmConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class KbLlmConfigServiceImpl implements KbLlmConfigService {

    @Resource
    private KbLlmProperties llm;

    @Override
    public KbLlmConfigVo getConfig() {
        KbLlmConfigVo vo = new KbLlmConfigVo();
        vo.setAvailable(llm.usable());
        vo.setConfigEnabled(llm.isEnabled());
        vo.setApiKeyConfigured(StringUtils.isNotBlank(llm.getApiKey()));
        vo.setProvider(llm.getProvider());
        vo.setModel(llm.getModel());
        return vo;
    }
}
