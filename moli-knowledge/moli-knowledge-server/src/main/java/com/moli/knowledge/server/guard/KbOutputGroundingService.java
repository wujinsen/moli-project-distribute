package com.moli.knowledge.server.guard;

import com.moli.knowledge.server.config.KbGuardrailsProperties;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.support.KbLlmCallScenes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * AI-9 Phase B · 单轮 Ask generative 输出 grounding 标注。
 */
@Slf4j
@Service
public class KbOutputGroundingService {

    @Resource
    private KbGuardrailsProperties guardrailsProperties;
    @Resource
    private KbGroundingSelfCheckSupport groundingSelfCheckSupport;

    public void applyGrounding(AskResponse resp, Long spaceId, InputGuardOutcome inputGuard) {
        if (!shouldApply(resp)) {
            return;
        }
        KbGroundingSelfCheckSupport.GroundingCheckResult check =
                groundingSelfCheckSupport.check(KbLlmCallScenes.ASK_GROUNDING, spaceId, resp);
        AskGuardVo guard = mergeGuardVo(inputGuard, check);
        resp.setGuard(guard);
    }

    public AskGuardVo mergeAgenticGuard(InputGuardOutcome inputGuard, boolean selfCheckRan,
                                       Double coverage, java.util.List<String> unsupported) {
        if (!guardrailsProperties.isEnabled()) {
            return inputGuard != null ? inputGuard.toVo() : null;
        }
        KbGroundingSelfCheckSupport.GroundingCheckResult synthetic = null;
        if (selfCheckRan) {
            synthetic = new KbGroundingSelfCheckSupport.GroundingCheckResult();
            synthetic.coverage = coverage;
            synthetic.unsupported = unsupported != null ? unsupported : new ArrayList<>();
            synthetic.parseFailed = coverage == null;
        }
        return mergeGuardVo(inputGuard, synthetic);
    }

    public AskGuardVo mergeGuardVo(InputGuardOutcome inputGuard,
                                   KbGroundingSelfCheckSupport.GroundingCheckResult grounding) {
        AskGuardVo guard = inputGuard != null && inputGuard.toVo() != null
                ? copyGuard(inputGuard.toVo()) : new AskGuardVo();
        if (inputGuard != null && inputGuard.isBypassed() && grounding == null) {
            return null;
        }
        if (grounding != null) {
            guard.setGroundingApplied(true);
            if (grounding.parseFailed) {
                guard.setCoverage(null);
                guard.setGroundingLow(null);
            } else {
                guard.setCoverage(grounding.coverage);
                double threshold = guardrailsProperties.getGrounding().getLowThreshold();
                guard.setGroundingLow(grounding.coverage != null && grounding.coverage < threshold);
            }
            guard.setUnsupportedStatements(
                    grounding.unsupported != null ? new ArrayList<>(grounding.unsupported) : new ArrayList<>());
        }
        return guard;
    }

    private boolean shouldApply(AskResponse resp) {
        if (resp == null || !"generative".equals(resp.getMode())) {
            return false;
        }
        return guardrailsProperties.isEnabled() && guardrailsProperties.getGrounding().isEnabled();
    }

    private static AskGuardVo copyGuard(AskGuardVo source) {
        AskGuardVo copy = new AskGuardVo();
        copy.setBlocked(source.isBlocked());
        copy.setFlagged(source.isFlagged());
        copy.setBlockReason(source.getBlockReason());
        copy.setPiiRedacted(source.isPiiRedacted());
        copy.setPiiTypes(source.getPiiTypes() != null ? new ArrayList<>(source.getPiiTypes()) : new ArrayList<>());
        return copy;
    }
}
