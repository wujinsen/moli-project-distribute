package com.moli.knowledge.server.guard;

import com.moli.knowledge.server.config.KbGuardrailsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * AI-9 输入 Guard：注入检测 + PII 脱敏（§1.2–§1.4）。
 */
@Slf4j
@Service
public class KbInputGuardService {

    @Resource
    private KbGuardrailsProperties guardrailsProperties;
    @Resource
    private KbInjectDetector injectDetector;
    @Resource
    private KbPiiRedactor piiRedactor;

    public InputGuardOutcome process(String rawQuestion) {
        String question = rawQuestion == null ? "" : rawQuestion.trim();
        if (!guardrailsProperties.isEnabled()) {
            return InputGuardOutcome.bypass(question);
        }
        try {
            KbInjectDetector.InjectDetectResult inject = guardrailsProperties.getInject().isEnabled()
                    ? injectDetector.detect(question)
                    : new KbInjectDetector.InjectDetectResult(InjectSeverity.PASS, null);

            KbPiiRedactor.PiiRedactResult pii = guardrailsProperties.getPii().isEnabled()
                    ? piiRedactor.redact(question, guardrailsProperties.normalizedPiiTypes())
                    : new KbPiiRedactor.PiiRedactResult(question, java.util.Collections.emptyList(), false);

            if (pii.isTooShortAfterRedact()) {
                return InputGuardOutcome.piiOnlyReject(pii.getRedactedText(), pii.getTypes());
            }

            boolean piiRedacted = !pii.getTypes().isEmpty();
            String redacted = pii.getRedactedText();

            if (inject.getSeverity() == InjectSeverity.BLOCK) {
                return InputGuardOutcome.blocked(inject.getRuleId(), redacted, pii.getTypes(), piiRedacted);
            }
            if (inject.getSeverity() == InjectSeverity.FLAG) {
                return InputGuardOutcome.flagged(inject.getRuleId(), redacted, pii.getTypes(), piiRedacted);
            }
            return InputGuardOutcome.pass(redacted, pii.getTypes(), piiRedacted);
        } catch (Exception e) {
            log.warn("[kb-guardrails] inject/pii engine error: {}", e.getMessage());
            if (guardrailsProperties.getInject().isFailOpen()) {
                return InputGuardOutcome.failOpenFlagged(question);
            }
            throw e;
        }
    }

    public boolean isEnabled() {
        return guardrailsProperties.isEnabled();
    }

    public String mergeBlockedAnswer(String optionalRetrievalTail) {
        if (StringUtils.isBlank(optionalRetrievalTail)) {
            return InputGuardOutcome.BLOCK_ANSWER;
        }
        return InputGuardOutcome.BLOCK_ANSWER + "\n\n" + optionalRetrievalTail;
    }
}
