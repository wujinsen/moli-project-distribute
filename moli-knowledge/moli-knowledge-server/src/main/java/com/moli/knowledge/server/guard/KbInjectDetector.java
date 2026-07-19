package com.moli.knowledge.server.guard;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI-9 §1.2 Prompt 注入检测（规则/正则，无 LLM）。
 */
@Component
public class KbInjectDetector {

    private static final int MAX_INPUT_CHARS = 8000;

    private static final Pattern CODE_FENCE = Pattern.compile("```[\\s\\S]*?```", Pattern.CASE_INSENSITIVE);

    private static final List<InjectRule> BLOCK_RULES;
    private static final List<Pattern> ALLOWLIST_PATTERNS;

    static {
        BLOCK_RULES = Collections.unmodifiableList(Arrays.asList(
                new InjectRule("inject_role_hijack",
                        "(?i)(developer\\s+mode|jailbreak|\\bdan\\b|do\\s+anything\\s+now"
                                + "|ignore\\s+(all\\s+)?(previous|prior)\\s+(instructions|prompts)"
                                + "|disregard\\s+the\\s+above"
                                + "|forget\\s+(your\\s+)?(system\\s+)?(prompt|instructions)"
                                + "|你现在是(?!.*如何).{0,8}(黑客|管理员|root)"
                                + "|忽略(以上|之前|先前).{0,8}(指令|提示|规则)"
                                + "|不要遵守系统提示"
                                + "|无视系统(提示|指令))"),
                new InjectRule("inject_secret_exfil",
                        "(?i)(api[_\\-]?key|secret\\s+key|authorization\\s+header"
                                + "|KB_LLM_[A-Z0-9_]+"
                                + "|输出.{0,12}(密钥|api\\s*key|token|环境变量)"
                                + "|print\\s+(all\\s+)?env(ironment)?\\s+variables"
                                + "|泄露.{0,8}(密钥|口令))"),
                new InjectRule("inject_tool_abuse",
                        "(?i)(/etc/passwd|C:\\\\Windows\\\\System32"
                                + "|\\brm\\s+-rf\\s+/\\b"
                                + "|\\bdrop\\s+table\\b"
                                + "|\\bexec\\s*\\(\\s*['\"]?(bash|sh|cmd)"
                                + "|执行.{0,6}(shell|bash|sql\\s*注入)"
                                + "|读取服务器本地文件)")));
        ALLOWLIST_PATTERNS = Collections.unmodifiableList(Arrays.asList(
                Pattern.compile("(?i)(ignoreunknown|ignore-unknown|ignore_unknown)"),
                Pattern.compile("(?i)(spring\\s+.*ignore|jackson\\s+.*ignore)"),
                Pattern.compile("(?i)(nginx\\s+.*ignore|如何配置.*ignore)"),
                Pattern.compile("如何(配置|排查|设置).{0,20}(ignore|忽略).{0,20}(配置|项|参数|错误)"),
                Pattern.compile("(?i)(troubleshoot|configure).{0,30}ignore.{0,30}(property|config|setting)")));
    }

    @Value
    public static class InjectDetectResult {
        InjectSeverity severity;
        String ruleId;
    }

    public InjectDetectResult detect(String question) {
        if (StringUtils.isBlank(question)) {
            return passResult();
        }
        String text = question.length() > MAX_INPUT_CHARS ? question.substring(0, MAX_INPUT_CHARS) : question;
        String outsideCode = stripCodeFences(text);
        boolean allowlisted = matchesAllowlist(outsideCode) && !matchesRoleHijack(outsideCode);

        InjectDetectResult worstInCode = passResult();
        Matcher codeMatcher = CODE_FENCE.matcher(text);
        while (codeMatcher.find()) {
            String block = codeMatcher.group();
            InjectDetectResult inCode = evaluateRules(block, false);
            worstInCode = maxSeverity(worstInCode, inCode);
        }

        if (allowlisted) {
            if (worstInCode.severity == InjectSeverity.BLOCK && matchesRoleHijack(outsideCode)) {
                return worstInCode;
            }
            if (worstInCode.severity == InjectSeverity.BLOCK) {
                return new InjectDetectResult(InjectSeverity.FLAG, worstInCode.ruleId);
            }
            return passResult();
        }

        InjectDetectResult outside = evaluateRules(outsideCode, true);
        InjectDetectResult merged = maxSeverity(outside, downgradeBlockToFlag(worstInCode));
        if (merged.severity == InjectSeverity.BLOCK) {
            return merged;
        }
        if (merged.severity == InjectSeverity.FLAG) {
            return merged;
        }
        return passResult();
    }

    private static InjectDetectResult evaluateRules(String text, boolean allowBlock) {
        for (InjectRule rule : BLOCK_RULES) {
            if (rule.pattern.matcher(text).find()) {
                return new InjectDetectResult(allowBlock ? InjectSeverity.BLOCK : InjectSeverity.FLAG, rule.id);
            }
        }
        return passResult();
    }

    private static InjectDetectResult downgradeBlockToFlag(InjectDetectResult result) {
        if (result.severity == InjectSeverity.BLOCK) {
            return new InjectDetectResult(InjectSeverity.FLAG, result.ruleId);
        }
        return result;
    }

    private static InjectDetectResult maxSeverity(InjectDetectResult a, InjectDetectResult b) {
        if (a.severity == InjectSeverity.BLOCK || b.severity == InjectSeverity.BLOCK) {
            String ruleId = a.severity == InjectSeverity.BLOCK ? a.ruleId : b.ruleId;
            return new InjectDetectResult(InjectSeverity.BLOCK, ruleId);
        }
        if (a.severity == InjectSeverity.FLAG || b.severity == InjectSeverity.FLAG) {
            String ruleId = a.severity == InjectSeverity.FLAG ? a.ruleId : b.ruleId;
            return new InjectDetectResult(InjectSeverity.FLAG, ruleId);
        }
        return passResult();
    }

    private static boolean matchesAllowlist(String text) {
        for (Pattern p : ALLOWLIST_PATTERNS) {
            if (p.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesRoleHijack(String text) {
        InjectRule roleRule = BLOCK_RULES.get(0);
        return roleRule.pattern.matcher(text).find();
    }

    private static String stripCodeFences(String text) {
        return CODE_FENCE.matcher(text).replaceAll(" ");
    }

    private static final class InjectRule {
        private final String id;
        private final Pattern pattern;

        private InjectRule(String id, String regex) {
            this.id = id;
            this.pattern = Pattern.compile(regex);
        }
    }

    private static InjectDetectResult passResult() {
        return new InjectDetectResult(InjectSeverity.PASS, null);
    }
}
