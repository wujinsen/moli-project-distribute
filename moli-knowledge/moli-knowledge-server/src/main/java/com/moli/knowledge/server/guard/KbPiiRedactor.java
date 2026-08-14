package com.moli.knowledge.server.guard;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AI-9 §1.4 PII 识别与脱敏。
 */
@Component
public class KbPiiRedactor {

    private static final Pattern EMAIL =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE =
            Pattern.compile("(?<!\\d)(1[3-9]\\d{9})(?!\\d)");
    private static final Pattern ID_CARD =
            Pattern.compile("(?<!\\d)([1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx])(?!\\d)");

    @Value
    public static class PiiRedactResult {
        String redactedText;
        List<String> types;
        boolean tooShortAfterRedact;
    }

    public PiiRedactResult redact(String text, List<String> enabledTypes) {
        if (StringUtils.isBlank(text)) {
            return new PiiRedactResult(text, new ArrayList<>(), false);
        }
        Set<String> types = new LinkedHashSet<>();
        String out = text;
        List<String> normalized = enabledTypes == null ? new ArrayList<String>() : enabledTypes;

        if (normalized.contains("email")) {
            if (EMAIL.matcher(out).find()) {
                types.add("email");
            }
            out = EMAIL.matcher(out).replaceAll("[EMAIL]");
        }
        if (normalized.contains("phone")) {
            if (PHONE.matcher(out).find()) {
                types.add("phone");
            }
            out = PHONE.matcher(out).replaceAll("[PHONE]");
        }
        if (normalized.contains("id_card")) {
            if (ID_CARD.matcher(out).find()) {
                types.add("id_card");
            }
            out = ID_CARD.matcher(out).replaceAll("[ID_CARD]");
        }

        boolean tooShort = effectiveLength(out) < 2;
        return new PiiRedactResult(out, new ArrayList<>(types), tooShort && !types.isEmpty());
    }

    private static int effectiveLength(String text) {
        if (text == null) {
            return 0;
        }
        String stripped = text.replaceAll("\\[EMAIL]|\\[PHONE]|\\[ID_CARD]", "")
                .replaceAll("\\s+", "")
                .trim();
        return stripped.length();
    }
}
