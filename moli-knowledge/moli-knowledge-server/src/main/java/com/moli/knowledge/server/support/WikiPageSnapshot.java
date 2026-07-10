package com.moli.knowledge.server.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** wiki 磁盘页快照（KBOPS-A3 漂移检测）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageSnapshot {

    private String slug;
    private String contentHash;
    private String relativePath;
}
