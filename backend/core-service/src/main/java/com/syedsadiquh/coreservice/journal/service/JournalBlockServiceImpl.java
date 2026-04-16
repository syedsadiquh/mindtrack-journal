package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.request.ReorderBlocksRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalBlockResponse;
import com.syedsadiquh.coreservice.journal.util.JournalBlockMapper;
import com.syedsadiquh.coreservice.journal.entity.BlockVersion;
import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import com.syedsadiquh.coreservice.journal.entity.JournalPage;
import com.syedsadiquh.coreservice.journal.event.PageAnalysisEvent;
import com.syedsadiquh.coreservice.journal.exception.JournalBadRequestException;
import com.syedsadiquh.coreservice.journal.exception.JournalException;
import com.syedsadiquh.coreservice.journal.exception.JournalNotFoundException;
import com.syedsadiquh.coreservice.journal.repository.BlockVersionRepository;
import com.syedsadiquh.coreservice.journal.repository.JournalBlockRepository;
import com.syedsadiquh.coreservice.journal.repository.JournalPageRepository;
import com.syedsadiquh.coreservice.journal.util.BlockTextUtil;
import com.syedsadiquh.coreservice.journal.util.SanitizerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.HashSet.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalBlockServiceImpl implements JournalBlockService {

    private final JournalPageRepository pageRepository;
    private final JournalBlockRepository blockRepository;
    private final BlockVersionRepository blockVersionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SanitizerUtil sanitizerUtil;

    @Transactional("journalTransactionManager")
    @Override
    public JournalBlockResponse addBlock(UUID userId, UUID pageId, CreateBlockRequest request) {
        try {
            JournalPage page = pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            JournalBlock parentBlock = null;
            if (request.getParentBlockId() != null) {
                parentBlock = blockRepository.findByIdAndPageIdAndDeletedFalse(request.getParentBlockId(), pageId)
                        .orElseThrow(() -> new JournalNotFoundException("Parent block not found: " + request.getParentBlockId()));
            }

            int orderIndex = request.getOrderIndex() != null
                    ? request.getOrderIndex()
                    : blockRepository.countByPageIdAndDeletedFalse(pageId);

            Map<String, Object> sanitizedContent = sanitizerUtil.sanitizeMap(request.getContent());
            Map<String, Object> sanitizedMetadata = sanitizerUtil.sanitizeMap(request.getMetadata());

            JournalBlock block = JournalBlock.builder()
                    .tenantId(page.getTenantId())
                    .page(page)
                    .parentBlock(parentBlock)
                    .type(request.getType())
                    .orderIndex(orderIndex)
                    .content(sanitizedContent)
                    .metadata(sanitizedMetadata)
                    .createdBy(userId.toString())
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build();

            JournalBlock saved = blockRepository.save(block);

            publishPageAnalysisIfText(pageId, sanitizedContent);

            log.info("Block added to page {}: {} (type: {})", pageId, saved.getId(), saved.getType());
            return JournalBlockMapper.toResponse(saved);
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to add block to page {}: {}", pageId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to add block. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public JournalBlockResponse updateBlock(UUID userId, UUID pageId, UUID blockId, UpdateBlockRequest request) {
        try {
            pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            JournalBlock block = blockRepository.findByIdAndPageIdAndDeletedFalse(blockId, pageId)
                    .orElseThrow(() -> new JournalNotFoundException("Block not found: " + blockId));

            // Save version snapshot before updating
            int versionNumber = blockVersionRepository.countByBlockId(blockId) + 1;
            BlockVersion version = BlockVersion.builder()
                    .block(block)
                    .versionNumber(versionNumber)
                    .content(block.getContent())
                    .metadata(block.getMetadata())
                    .createdBy(userId.toString())
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build();
            blockVersionRepository.save(version);

            boolean contentChanged = false;
            if (request.getType() != null) block.setType(request.getType());
            if (request.getOrderIndex() != null) block.setOrderIndex(request.getOrderIndex());
            if (request.getContent() != null) {
                Map<String, Object> sanitizedContent = sanitizerUtil.sanitizeMap(request.getContent());
                block.setContent(sanitizedContent);
                contentChanged = true;
            }
            if (request.getMetadata() != null) {
                Map<String, Object> sanitizedMetadata = sanitizerUtil.sanitizeMap(request.getMetadata());
                block.setMetadata(sanitizedMetadata);
            }

            block.setUpdatedBy(userId.toString());
            block.setUpdatedAt(LocalDateTime.now());

            JournalBlock updated = blockRepository.save(block);

            if (contentChanged) {
                publishPageAnalysisIfText(pageId, updated.getContent());
            }

            log.info("Block updated: {} for page: {}", blockId, pageId);
            return JournalBlockMapper.toResponse(updated);
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update block {}: {}", blockId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to update block. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public void softDeleteBlock(UUID userId, UUID pageId, UUID blockId) {
        try {
            pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            JournalBlock block = blockRepository.findByIdAndPageIdAndDeletedFalse(blockId, pageId)
                    .orElseThrow(() -> new JournalNotFoundException("Block not found: " + blockId));

            block.setDeleted(true);
            block.setDeletedBy(userId.toString());
            block.setDeletedAt(LocalDateTime.now());
            blockRepository.save(block);

            publishPageAnalysisIfText(pageId, block.getContent());

            log.info("Block soft-deleted: {} from page: {}", blockId, pageId);
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to soft-delete block {}: {}", blockId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to delete block. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public List<JournalBlockResponse> reorderBlocks(UUID userId, UUID pageId, ReorderBlocksRequest request) {
        try {
            pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            List<JournalBlock> blocks = blockRepository.findByPageIdAndDeletedFalseOrderByOrderIndexAsc(pageId);
            Set<UUID> existingIds = new HashSet<>();
            Map<UUID, JournalBlock> blockMap = new HashMap<>();
            for (JournalBlock b : blocks) {
                existingIds.add(b.getId());
                blockMap.put(b.getId(), b);
            }

            List<UUID> orderedIds = request.getBlockIds();

            // Reject duplicates in the request
            Set<UUID> seen = newHashSet(orderedIds.size());
            for (UUID id : orderedIds) {
                if (!seen.add(id)) {
                    throw new JournalBadRequestException("Duplicate block ID in reorder request: " + id);
                }
            }

            // Reject unknown IDs
            Set<UUID> requested = new HashSet<>(orderedIds);
            Set<UUID> unknown = new HashSet<>(requested);
            unknown.removeAll(existingIds);
            if (!unknown.isEmpty()) {
                throw new JournalBadRequestException("Reorder request contains IDs not belonging to this page: " + unknown);
            }

            // Reject missing IDs (the request must cover every live block)
            Set<UUID> missing = new HashSet<>(existingIds);
            missing.removeAll(requested);
            if (!missing.isEmpty()) {
                throw new JournalBadRequestException("Reorder request is missing block IDs: " + missing);
            }

            // Input is a valid permutation — apply deterministic 0-based indexes
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < orderedIds.size(); i++) {
                JournalBlock block = blockMap.get(orderedIds.get(i));
                block.setOrderIndex(i);
                block.setUpdatedBy(userId.toString());
                block.setUpdatedAt(now);
            }

            List<JournalBlock> saved = blockRepository.saveAll(blocks);
            log.info("Blocks reordered: {} for page: {}", orderedIds, pageId);
            return saved.stream()
                    .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                    .map(JournalBlockMapper::toResponse)
                    .toList();
        } catch (JournalNotFoundException | JournalBadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to reorder blocks: {}", e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to reorder blocks. Please try again.");
        }
    }

    private void publishPageAnalysisIfText(UUID pageId, Map<String, Object> content) {
        if (BlockTextUtil.hasText(content)) {
            eventPublisher.publishEvent(new PageAnalysisEvent(pageId));
        }
    }

}
