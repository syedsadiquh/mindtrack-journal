package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.response.TagResponse;

import java.util.List;
import java.util.UUID;

public interface TagService {

    TagResponse createTag(UUID userId, CreateTagRequest request);

    List<TagResponse> getTagsByTenant(UUID userId, UUID tenantId);

    TagResponse updateTag(UUID userId, UUID tagId, UUID tenantId, UpdateTagRequest request);

    void deleteTag(UUID userId, UUID tagId, UUID tenantId);
}
