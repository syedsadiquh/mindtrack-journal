package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.response.TagResponse;
import com.syedsadiquh.coreservice.journal.entity.Tag;
import com.syedsadiquh.coreservice.journal.exception.JournalException;
import com.syedsadiquh.coreservice.journal.exception.JournalNotFoundException;
import com.syedsadiquh.coreservice.journal.exception.TagAlreadyExistsException;
import com.syedsadiquh.coreservice.journal.exception.TenantAccessDeniedException;
import com.syedsadiquh.coreservice.journal.repository.TagRepository;
import com.syedsadiquh.coreservice.user.api.TenantMembershipService;
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
    private final TenantMembershipService tenantMembershipService;

    @Transactional("journalTransactionManager")
    @Override
    public TagResponse createTag(UUID userId, CreateTagRequest request) {
        try {
            requireMembership(userId, request.getTenantId());
            if (tagRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedFalse(request.getTenantId(), request.getName())) {
                throw new TagAlreadyExistsException("Tag with name '" + request.getName() + "' already exists");
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
        } catch (TenantAccessDeniedException | TagAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create tag {} for tenant {}: {}", request.getName(), request.getTenantId(), e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to create tag. Please try again.");
        }
    }

    @Override
    public List<TagResponse> getTagsByTenant(UUID userId, UUID tenantId) {
        try {
            requireMembership(userId, tenantId);
            return tagRepository.findByTenantIdAndDeletedFalse(tenantId).stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get tags for tenant {}: {}", tenantId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to get tags. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public TagResponse updateTag(UUID userId, UUID tagId, UUID tenantId, UpdateTagRequest request) {
        try {
            requireMembership(userId, tenantId);
            Tag tag = tagRepository.findByIdAndTenantIdAndDeletedFalse(tagId, tenantId)
                    .orElseThrow(() -> new JournalNotFoundException("Tag not found: " + tagId));

            if (request.getName() != null) {
                if (!tag.getName().equals(request.getName())
                        && tagRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedFalse(tenantId, request.getName())) {
                    throw new TagAlreadyExistsException("Tag with name '" + request.getName() + "' already exists");
                }
                tag.setName(request.getName());
            }
            if (request.getColor() != null) tag.setColor(request.getColor());

            tag.setUpdatedBy(userId.toString());
            tag.setUpdatedAt(LocalDateTime.now());

            Tag updated = tagRepository.save(tag);
            return toResponse(updated);
        } catch (TenantAccessDeniedException | JournalNotFoundException | TagAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update tag {} for tenant {}: {}", tagId, tenantId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to update tag. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public void deleteTag(UUID userId, UUID tagId, UUID tenantId) {
        try {
            requireMembership(userId, tenantId);
            Tag tag = tagRepository.findByIdAndTenantIdAndDeletedFalse(tagId, tenantId)
                    .orElseThrow(() -> new JournalNotFoundException("Tag not found: " + tagId));

            tag.setDeleted(true);
            tag.setDeletedBy(userId.toString());
            tag.setDeletedAt(LocalDateTime.now());
            tagRepository.save(tag);

            log.info("Tag soft-deleted: {} by user: {}", tagId, userId);
        } catch (TenantAccessDeniedException | JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete tag {} for tenant {}: {}", tagId, tenantId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to delete tag. Please try again.");
        }
    }

    private void requireMembership(UUID userId, UUID tenantId) {
        if (!tenantMembershipService.isMember(tenantId, userId)) {
            throw new TenantAccessDeniedException("Access denied: user is not a member of tenant " + tenantId);
        }
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
