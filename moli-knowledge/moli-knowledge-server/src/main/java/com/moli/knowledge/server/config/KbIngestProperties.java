package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Web Ingest 工作台（T15）配置。
 *
 * <p>raw 树只读浏览 + 批次 Plan 规划；草稿/落盘见 T15b/c。
 * <ul>
 *   <li>{@code rawRoot}：只读 raw 根目录（相对进程 cwd 或绝对路径），默认 {@code kb/} 根下 {@code raw}。</li>
 *   <li>{@code maxPagesPerBatch}：单批次 Plan 允许规划的最大页数（与 AGENTS §4「5–15 页」一致）。</li>
 * </ul>
 * wiki 根与 space→目录映射复用 {@link KbWikiProperties}（{@code kb.wiki.*}）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.ingest")
public class KbIngestProperties {

    /** 是否启用 Ingest 工作台 API。 */
    private boolean enabled = true;

    /** 只读 raw 根目录（相对项目根或绝对路径）。 */
    private String rawRoot = "moli-knowledge/kb/raw";

    /** 单批次 Plan 最大页数（create + enrich 合计的软上限）。 */
    private int maxPagesPerBatch = 15;

    /** raw-tree 单次返回的最大节点数，防止超大目录拖垮响应。 */
    private int maxTreeNodes = 5000;

    /** Plan 生成时单个 raw 文件喂给 LLM 的最大字符数（截断）。 */
    private int rawSnippetChars = 4000;

    /**
     * 模板模式（useLlmGenerate=false）读 raw 正文的最大字符数。
     * {@code 0} 表示不截断（直贴全文）；正数为安全上限。
     */
    private int templateRawMaxChars = 0;

    /** raw-coverage wiki 索引内存缓存 TTL（秒）。 */
    private int coverageCacheSeconds = 300;

    /** raw-coverage 单次返回的最大文件项数（防超大目录）。 */
    private int maxCoverageFiles = 10000;

    /**
     * Expert {@code POST .../commit} 未传 {@code sync} 时是否自动触发 wiki→MySQL Sync。
     * Express {@code publish} 默认仍为 sync=true；显式 {@code sync=false} 可跳过。
     */
    private boolean commitAutoSync = true;

    /** T20a · 单文件 raw 上传大小上限（字节）。 */
    private long rawUploadMaxBytes = 5L * 1024 * 1024;

    /** T20a · 单次 raw 上传最多文件数。 */
    private int rawUploadMaxFiles = 20;

    /** T20c · zip 投喂大小上限（字节）。 */
    private long rawUploadZipMaxBytes = 50L * 1024 * 1024;

    /** T20c · zip 内最多解压文件数。 */
    private int rawUploadZipMaxEntries = 200;

    /** T15f · 异步 generate + SSE。 */
    private Generate generate = new Generate();

    @Data
    public static class Generate {
        /** 是否允许 POST .../generate/start + SSE stream。 */
        private boolean asyncEnabled = true;

        private int corePoolSize = 2;
        private int maxPoolSize = 4;
        private int queueCapacity = 8;

        /** SseEmitter 超时（毫秒）。 */
        private long sseTimeoutMs = 600_000L;

        /** 终态任务在内存中保留时长（毫秒），供断线重连查询。 */
        private long taskTtlMs = 1_800_000L;
    }
}
