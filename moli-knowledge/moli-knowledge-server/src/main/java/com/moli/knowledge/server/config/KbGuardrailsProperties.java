package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI-9 Guardrails（§3 kb.guardrails.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.guardrails")
public class KbGuardrailsProperties {

    private boolean enabled = false;
    private Inject inject = new Inject();
    private Pii pii = new Pii();
    private Grounding grounding = new Grounding();

    public List<String> normalizedPiiTypes() {
        if (pii.types == null || pii.types.isEmpty()) {
            return Arrays.asList("email", "phone", "id_card");
        }
        List<String> out = new ArrayList<>();
        for (String t : pii.types) {
            if (t != null && !t.trim().isEmpty()) {
                out.add(t.trim().toLowerCase());
            }
        }
        return out.isEmpty() ? Arrays.asList("email", "phone", "id_card") : out;
    }

    @Data
    public static class Inject {
        private boolean enabled = true;
        private boolean failOpen = true;
    }

    @Data
    public static class Pii {
        private boolean enabled = true;
        private List<String> types = new ArrayList<>(Arrays.asList("email", "phone", "id_card"));
    }

    @Data
    public static class Grounding {
        private boolean enabled = true;
        private double lowThreshold = 0.8;
        private int timeoutMs = 8000;
    }
}
