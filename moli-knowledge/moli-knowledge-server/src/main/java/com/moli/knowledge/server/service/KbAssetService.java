package com.moli.knowledge.server.service;

import javax.servlet.http.HttpServletResponse;

/**
 * T22 R0：raw / wiki inline 图片只读接口。
 */
public interface KbAssetService {

    /**
     * 读取 raw 树下图片（相对 {@code kb.ingest.raw-root}）。
     *
     * @param spaceId 空间 ID（ACL）；省略时默认 enterprise-kb
     * @param path    相对 raw 根的路径，如 {@code wujinsen_markdown/.../imageFile1.png}
     */
    void serveRawAsset(Long spaceId, String path, HttpServletResponse response);

    /**
     * 读取 wiki 页旁路 {@code {slug}.assets/} 下图片。
     *
     * @param spaceId 空间 ID（ACL）
     * @param slug    wiki 全路径 slug，如 {@code bigdata/flink-流批一体入门}
     * @param rel     相对 asset 目录的路径，如 {@code imageFile1.png} 或 {@code assets/imageFile1.png}
     */
    void serveWikiAsset(Long spaceId, String slug, String rel, HttpServletResponse response);
}
