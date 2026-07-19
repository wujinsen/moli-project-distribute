package com.moli.knowledge.server.guard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.service.KbLlmClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * AI-7 §3.3 / AI-9 输出 grounding 自检（陈述 ↔ citations 对齐）。
 */
@Component
public class KbGroundingSelfCheckSupport {

    public static final String SELF_CHECK_SYSTEM =
            "你是答案核查器。给定【答案】与【可用引用页(slug+片段)】，把答案拆成原子陈述句，逐句判定是否被某条引用内容直接支撑。\n"
                    + "规则：只依据给定引用判断；不确定或引用中找不到依据的，判为 unsupported。\n"
                    + "对每条 unsupported，给出“还需要检索什么信息”的关键词（用于回补检索）。\n"
                    + "只输出 JSON：{\"supported\":[\"陈述…\"],\"unsupported\":[\"陈述…\"],\"missingInfo\":[\"关键词…\"]}";

    @Resource
    private KbLlmClient kbLlmClient;

    public GroundingCheckResult check(String scene, Long spaceId, AskResponse generated) {
        String userPrompt = buildUserPrompt(generated);
        try {
            String raw = kbLlmClient.chat(scene, spaceId, SELF_CHECK_SYSTEM, userPrompt);
            return parseSelfCheckJson(raw);
        } catch (Exception e) {
            GroundingCheckResult failed = new GroundingCheckResult();
            failed.parseFailed = true;
            failed.coverage = null;
            return failed;
        }
    }

    public static String buildUserPrompt(AskResponse generated) {
        StringBuilder citeCtx = new StringBuilder();
        if (generated.getCitations() != null) {
            for (AskResponse.Citation c : generated.getCitations()) {
                citeCtx.append("- [[")
                        .append(c.getSlug() == null ? "" : c.getSlug())
                        .append("]] ")
                        .append(c.getSnippet() == null ? "" : c.getSnippet())
                        .append("\n");
            }
        }
        return "【答案】\n"
                + (generated.getAnswer() == null ? "" : generated.getAnswer())
                + "\n\n【可用引用页】\n"
                + citeCtx;
    }

    public static GroundingCheckResult parseSelfCheckJson(String raw) {
        GroundingCheckResult result = new GroundingCheckResult();
        if (StringUtils.isBlank(raw)) {
            result.parseFailed = true;
            result.coverage = null;
            return result;
        }
        String json = raw.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }
        try {
            JSONObject obj = JSON.parseObject(json);
            result.supported = readStringArray(obj.getJSONArray("supported"));
            result.unsupported = readStringArray(obj.getJSONArray("unsupported"));
            result.missingInfo = readStringArray(obj.getJSONArray("missingInfo"));
            result.coverage = computeCoverage(result.supported, result.unsupported);
            return result;
        } catch (Exception e) {
            result.parseFailed = true;
            result.coverage = null;
            return result;
        }
    }

    public static double computeCoverage(List<String> supported, List<String> unsupported) {
        int sup = supported == null ? 0 : supported.size();
        int uns = unsupported == null ? 0 : unsupported.size();
        int total = sup + uns;
        if (total == 0) {
            return 1.0;
        }
        return (double) sup / total;
    }

    private static List<String> readStringArray(JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) {
            return out;
        }
        for (int i = 0; i < arr.size(); i++) {
            String s = arr.getString(i);
            if (StringUtils.isNotBlank(s)) {
                out.add(s.trim());
            }
        }
        return out;
    }

    public static final class GroundingCheckResult {
        public List<String> supported = new ArrayList<>();
        public List<String> unsupported = new ArrayList<>();
        public List<String> missingInfo = new ArrayList<>();
        public Double coverage;
        public boolean parseFailed;
    }
}
