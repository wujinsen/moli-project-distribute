package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.dto.WikiSaveResultVo;

/**
 * Web Wiki 在线编辑（T14a）：读写部署机上的 {@code kb/wiki*} markdown 文件。
 *
 * <p>铁律：保存目标是 <b>wiki 文件</b>（权威源），不直接写 {@code kb_document}；
 * 写盘后再走 Sync 才进库。读写均需空间 editor 权限。
 */
public interface KbWikiFileService {

    /** 读 wiki 文件全文（含 frontmatter）。文件不存在时返回 exists=false 的空壳，便于新建。 */
    WikiPageVo readPage(String slug, Long spaceId);

    /** 写 wiki 文件（必要时新建父目录）；可带 baselineHash 做乐观锁。 */
    WikiSaveResultVo writePage(WikiSaveRequest request);

    /**
     * 移动 wiki 文件到另一目录（=换分类）：移动 .md + 自动改其它页/edges 中的全路径引用，
     * newType 非空时同步改本页 frontmatter 的 type。返回新 slug。调用方负责随后触发 Sync。
     */
    WikiSaveResultVo movePage(Long spaceId, String fromSlug, String toSlug);
}
