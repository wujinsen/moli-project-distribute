package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiSpaceLintRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintVo;

/**
 * 空间级文件 Lint（文件真值）。
 *
 * <p>区别于 {@code KbInsightService.lint}（扫 MySQL {@code kb_document} 旧快照）：本服务直接调
 * {@code kb/tools/lint.py --wiki-dir {spaceDir} --json}，扫描部署机磁盘上的 wiki markdown，
 * 作为治理工作台「改完未 Sync 也能检」的真值入口。每条问题的 {@code page} 即 slug，
 * 可直接作为 enrich / ai-revise 的修复目标。
 */
public interface KbWikiLintService {

    WikiSpaceLintVo lintSpace(WikiSpaceLintRequest request);
}
