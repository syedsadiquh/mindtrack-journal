package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.response.TagResponse;
import com.syedsadiquh.coreservice.journal.entity.Tag;
import com.syedsadiquh.coreservice.journal.exception.JournalException;
import com.syedsadiquh.coreservice.journal.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Transactional("journalTransactionManager")
    @Override
    public TagResponse createTag(UUID userId, CreateTagRequest request) {
        if (tagRepository.existsByTenantIdAndNameAndDeletedFalse(request.getTenantId(), request.getName())) {
            throw new JournalException("Tag with name '" + request.getName() + "' already exists");
        }

        Tag tag = Tag.builder()
                .tenantId(request.getTenantId())
                .name(request.getName())
                .color(request.getColor())
                .createdBy(userId.toString())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        Tag saved = tagRepository.save(tag);
        log.info("Tag created: {} for tenant: {}", saved.getId(), request.getTenantId());
        return toResponse(saved);
    }

    @Override
    public List<TagResponse> getTagsByTenant(UUID tenantId) {
        return tagRepository.findByTenantIdAndDeletedFalse(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional("journalTransactionManager")
    @Override
    public TagResponse updateTag(UUID userId, UUID tagId, UUID tenantId, UpdateTagRequest request) {
        Tag tag = tagRepository.findByIdAndTenantIdAndDeletedFalse(tagId, tenantId)
                .orElseThrow(() -> new JournalException("Tag not found: " + tagId));

        if (request.getName() != null) {
            if (!tag.getName().equals(request.getName())
                    && tagRepository.existsByTenantIdAndNameAndDeletedFalse(tenantId, request.getName())) {
                throw new JournalException("Tag with name '" + request.getName() + "' already exists");
            }
            tag.setName(request.getName());
        }
        if (request.getColor() != null) tag.setColor(request.getColor());

        tag.setUpdatedBy(userId.toString());
        tag.setUpdatedAt(LocalDateTime.now());

        Tag updated = tagRepository.save(tag);
        return toResponse(updated);
    }

    @Transactional("journalTransactionManager")
    @Override
    public void deleteTag(UUID userId, UUID tagId, UUID tenantId) {
        Tag tag = tagRepository.findByIdAndTenantIdAndDeletedFalse(tagId, tenantId)
                .orElseThrow(() -> new JournalException("Tag not found: " + tagId));

        tag.setDeleted(true);
        tag.setDeletedBy(userId.toString());
        tag.setDeletedAt(LocalDateTime.now());
        tagRepository.save(tag);

        log.info("Tag soft-deleted: {} by user: {}", tagId, userId);
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
