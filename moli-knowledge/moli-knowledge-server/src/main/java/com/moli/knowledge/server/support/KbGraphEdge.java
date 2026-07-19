package com.moli.knowledge.server.support;

import com.moli.knowledge.server.entity.KbRelation;
import lombok.Value;

/**
 * 图扩跳算法用的轻量边（单测可手工构造，生产由 mapper 映射）。
 */
@Value
public class KbGraphEdge {

    Long sourceDocId;
    Long targetDocId;
    String relationType;
    Integer weight;
    boolean inbound;

    public static KbGraphEdge outbound(KbRelation relation) {
        return new KbGraphEdge(
                relation.getSourceDocId(),
                relation.getTargetDocId(),
                relation.getRelationType(),
                relation.getWeight(),
                false);
    }

    public static KbGraphEdge inbound(KbRelation relation) {
        return new KbGraphEdge(
                relation.getSourceDocId(),
                relation.getTargetDocId(),
                relation.getRelationType(),
                relation.getWeight(),
                true);
    }
}
