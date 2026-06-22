package com.moli.knowledge.server.service;

import com.moli.knowledge.server.entity.KbTag;

import java.util.List;

public interface KbTagService {

    List<KbTag> listBySpace(Long spaceId);

    Long create(KbTag tag);

    void update(KbTag tag);

    void delete(Long id);
}
