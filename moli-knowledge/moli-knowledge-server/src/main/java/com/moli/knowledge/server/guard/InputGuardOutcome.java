package com.moli.knowledge.server.guard;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public final class InputGuardOutcome {

    public static final String BLOCK_ANSWER =
            "检测到不安全的指令或越权请求，已拒绝生成式作答。"
                    + "如需查阅知识库内容，请用正常业务问题描述您的需求。";
    public static final String PII_ONLY_ANSWER = "请勿仅提交敏感个人信息。请补充具体业务问题后重试。";

    private final boolean bypassed;
    private final boolean blocked;
    private final boolean flagged;
    private final boolean piiOnlyReject;
    private final String blockReason;
    private final String questionForProcessing;
    private final boolean piiRedacted;
    private final List<String> piiTypes;

    private InputGuardOutcome(boolean bypassed, boolean blocked, boolean flagged, boolean piiOnlyReject,
                              String blockReason, String questionForProcessing,
                              boolean piiRedacted, List<String> piiTypes) {
        this.bypassed = bypassed;
        this.blocked = blocked;
        this.flagged = flagged;
        this.piiOnlyReject = piiOnlyReject;
        this.blockReason = blockReason;
        this.questionForProcessing = questionForProcessing;
        this.piiRedacted = piiRedacted;
        this.piiTypes = piiTypes == null ? Collections.emptyList() : new ArrayList<>(piiTypes);
    }

    public static InputGuardOutcome bypass(String question) {
        return new InputGuardOutcome(true, false, false, false, null, question, false, Collections.emptyList());
    }

    public static InputGuardOutcome blocked(String ruleId, String redactedQuestion, List<String> piiTypes,
                                            boolean piiRedacted) {
        return new InputGuardOutcome(false, true, false, false, ruleId, redactedQuestion, piiRedacted, piiTypes);
    }

    public static InputGuardOutcome flagged(String ruleId, String redactedQuestion, List<String> piiTypes,
                                            boolean piiRedacted) {
        return new InputGuardOutcome(false, false, true, false, ruleId, redactedQuestion, piiRedacted, piiTypes);
    }

    public static InputGuardOutcome pass(String redactedQuestion, List<String> piiTypes, boolean piiRedacted) {
        return new InputGuardOutcome(false, false, false, false, null, redactedQuestion, piiRedacted, piiTypes);
    }

    public static InputGuardOutcome piiOnlyReject(String redactedQuestion, List<String> piiTypes) {
        return new InputGuardOutcome(false, false, false, true, null, redactedQuestion, true, piiTypes);
    }

    public static InputGuardOutcome failOpenFlagged(String question) {
        return new InputGuardOutcome(false, false, true, false, "inject_fail_open", question, false,
                Collections.emptyList());
    }

    public AskGuardVo toVo() {
        if (bypassed) {
            return null;
        }
        AskGuardVo vo = new AskGuardVo();
        vo.setBlocked(blocked);
        vo.setFlagged(flagged);
        vo.setBlockReason(blockReason);
        vo.setPiiRedacted(piiRedacted);
        vo.setPiiTypes(new ArrayList<>(piiTypes));
        return vo;
    }
}
