package com.moli.user.center.common.domain.dto.operation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 部署 serviceKey 与项目别名目录（默认内置；启动时由 {@code OperationDeployServiceRegistry} 用 YAML 覆盖）。
 */
public final class OperationDeployServiceCatalog {

    private static volatile CatalogState state = CatalogState.from(defaultEntries());

    private OperationDeployServiceCatalog() {
    }

    public static void install(List<OperationDeployServiceEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            state = CatalogState.from(defaultEntries());
            return;
        }
        state = CatalogState.from(entries);
    }

    public static List<OperationDeployServiceEntry> defaultEntries() {
        return Arrays.asList(
                entry("user-center", "用户中心",
                        "user-center", "moli-user-center", "user-center-server", "moli-server"),
                entry("gateway", "网关",
                        "gateway", "moli-gateway"),
                entry("knowledge", "知识库",
                        "knowledge", "moli-knowledge", "knowledge-server")
        );
    }

    public static Set<String> knownKeys() {
        return state.knownKeys;
    }

    public static boolean isKnownKey(String serviceKey) {
        String key = normalizeToken(serviceKey);
        return key != null && state.knownKeys.contains(key);
    }

    public static String requireKnownKey(String serviceKey) {
        String key = normalizeToken(serviceKey);
        if (key == null || !state.knownKeys.contains(key)) {
            throw new IllegalArgumentException("不支持的 serviceKey: " + serviceKey);
        }
        return key;
    }

    public static String resolveProjectName(String projectName) {
        if (StringUtils.isBlank(projectName)) {
            return null;
        }
        return state.projectToKey.get(normalizeProjectName(projectName));
    }

    public static List<OperationDeployServiceEntry> entries() {
        return state.entries;
    }

    private static OperationDeployServiceEntry entry(String key, String label, String... aliases) {
        OperationDeployServiceEntry e = new OperationDeployServiceEntry();
        e.setKey(key);
        e.setLabel(label);
        e.setAliases(new ArrayList<>(Arrays.asList(aliases)));
        return e;
    }

    private static String normalizeToken(String value) {
        return StringUtils.isBlank(value) ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeProjectName(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static final class CatalogState {
        private final List<OperationDeployServiceEntry> entries;
        private final Set<String> knownKeys;
        private final Map<String, String> projectToKey;

        private CatalogState(List<OperationDeployServiceEntry> entries,
                             Set<String> knownKeys,
                             Map<String, String> projectToKey) {
            this.entries = entries;
            this.knownKeys = knownKeys;
            this.projectToKey = projectToKey;
        }

        private static CatalogState from(List<OperationDeployServiceEntry> raw) {
            List<OperationDeployServiceEntry> entries = new ArrayList<>();
            Set<String> keys = new LinkedHashSet<>();
            Map<String, String> aliases = new LinkedHashMap<>();
            for (OperationDeployServiceEntry rawEntry : raw) {
                if (rawEntry == null || StringUtils.isBlank(rawEntry.getKey())) {
                    continue;
                }
                String key = normalizeToken(rawEntry.getKey());
                OperationDeployServiceEntry copy = new OperationDeployServiceEntry();
                copy.setKey(key);
                copy.setLabel(StringUtils.defaultIfBlank(rawEntry.getLabel(), key));
                List<String> aliasList = new ArrayList<>();
                aliasList.add(key);
                if (rawEntry.getAliases() != null) {
                    for (String alias : rawEntry.getAliases()) {
                        if (StringUtils.isNotBlank(alias)) {
                            aliasList.add(normalizeProjectName(alias));
                        }
                    }
                }
                copy.setAliases(aliasList);
                entries.add(copy);
                keys.add(key);
                for (String alias : aliasList) {
                    aliases.putIfAbsent(alias, key);
                }
            }
            return new CatalogState(
                    Collections.unmodifiableList(entries),
                    Collections.unmodifiableSet(keys),
                    Collections.unmodifiableMap(aliases));
        }
    }
}
