package com.moli.ai.server.bi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.ai.server.bi.config.BiChatProperties;
import com.moli.ai.server.bi.dto.BiSchemaColumnVo;
import com.moli.ai.server.bi.dto.BiSchemaTableVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GET /bi/chat/schema · 白名单表 + 列黑名单脱敏（INV-14）。
 */
@Slf4j
@Service
public class BiSchemaServiceImpl implements BiSchemaService {

    private final BiChatProperties properties;
    private final ObjectMapper objectMapper;
    private List<BiSchemaTableVo> cached = new ArrayList<>();

    public BiSchemaServiceImpl(BiChatProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadSchema() {
        Set<String> allow = normalizeAllowTables();
        List<BiSchemaTableVo> loaded = new ArrayList<>();
        try (InputStream in = new ClassPathResource("bi-chat/allow_tables.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            JsonNode tables = root.get("tables");
            if (tables != null && tables.isArray()) {
                for (JsonNode tableNode : tables) {
                    String table = text(tableNode, "table");
                    if (!allow.contains(table)) {
                        continue;
                    }
                    BiSchemaTableVo tableVo = new BiSchemaTableVo();
                    tableVo.setTable(table);
                    tableVo.setComment(text(tableNode, "comment"));
                    List<BiSchemaColumnVo> cols = new ArrayList<>();
                    JsonNode columns = tableNode.get("columns");
                    if (columns != null && columns.isArray()) {
                        for (JsonNode colNode : columns) {
                            String name = text(colNode, "name");
                            if (!StringUtils.hasText(name) || isDeniedColumn(name)) {
                                continue;
                            }
                            BiSchemaColumnVo col = new BiSchemaColumnVo();
                            col.setName(name);
                            col.setType(text(colNode, "type"));
                            col.setComment(text(colNode, "comment"));
                            cols.add(col);
                        }
                    }
                    tableVo.setColumns(cols);
                    loaded.add(tableVo);
                }
            }
        } catch (Exception ex) {
            log.warn("load bi-chat schema failed: {}", ex.getMessage());
        }
        cached = loaded;
    }

    @Override
    public List<BiSchemaTableVo> listAllowedSchema() {
        return cached;
    }

    private Set<String> normalizeAllowTables() {
        if (properties.getAllowTables() == null) {
            return new HashSet<>();
        }
        return properties.getAllowTables().stream()
                .filter(StringUtils::hasText)
                .map(t -> t.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toSet());
    }

    private boolean isDeniedColumn(String columnName) {
        String name = columnName.toLowerCase(Locale.ROOT);
        List<String> deny = properties.getDenyColumns();
        if (deny != null) {
            for (String d : deny) {
                if (!StringUtils.hasText(d)) {
                    continue;
                }
                String rule = d.toLowerCase(Locale.ROOT).trim();
                if (rule.endsWith("*")) {
                    if (name.startsWith(rule.substring(0, rule.length() - 1))) {
                        return true;
                    }
                } else if (name.equals(rule)) {
                    return true;
                }
            }
        }
        return name.endsWith("_key");
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
