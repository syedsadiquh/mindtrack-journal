package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.infrastructure.client.SentimentAnalyzerClient;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentRequest;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentResponse;
import com.syedsadiquh.coreservice.journal.dto.request.CreateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.request.CreateJournalPageRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateJournalPageRequest;
import com.syedsadiquh.coreservice.journal.dto.response.*;
import com.syedsadiquh.coreservice.journal.entity.*;
import com.syedsadiquh.coreservice.journal.event.PageAnalysisEvent;
import com.syedsadiquh.coreservice.journal.exception.JournalBadRequestException;
import com.syedsadiquh.coreservice.journal.exception.JournalException;
import com.syedsadiquh.coreservice.journal.exception.JournalNotFoundException;
import com.syedsadiquh.coreservice.journal.exception.TenantAccessDeniedException;
import com.syedsadiquh.coreservice.journal.repository.JournalBlockRepository;
import com.syedsadiquh.coreservice.journal.repository.JournalPageRepository;
import com.syedsadiquh.coreservice.journal.repository.TagRepository;
import com.syedsadiquh.coreservice.journal.util.BlockTextUtil;
import com.syedsadiquh.coreservice.journal.util.JournalBlockMapper;
import com.syedsadiquh.coreservice.journal.util.SanitizerUtil;
import com.syedsadiquh.coreservice.user.api.TenantMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalPageServiceImpl implements JournalPageService {

    private final JournalPageRepository pageRepository;
    private final JournalBlockRepository blockRepository;
    private final TagRepository tagRepository;
    private final TenantMembershipService tenantMembershipService;
    private final ApplicationEventPublisher eventPublisher;
    private final SanitizerUtil sanitizerUtil;
    private final SentimentAnalyzerClient sentimentAnalyzerClient;
    private final SentimentAnalysisService sentimentAnalysisService;

    @Transactional("journalTransactionManager")
    @Override
    public JournalPageDetailResponse createPage(UUID userId, CreateJournalPageRequest request) {
        try {
            requireMembership(userId, request.getTenantId());
            JournalPage page = JournalPage.builder()
                    .tenantId(request.getTenantId())
                    .userId(userId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .coverImageUrl(request.getCoverImageUrl())
                    .entryDate(request.getEntryDate())
                    .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : true)
                    .createdBy(userId.toString())
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build();

            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                List<UUID> tagIds = request.getTagIds();
                List<Tag> tags = tagRepository.findByIdInAndTenantIdAndDeletedFalse(tagIds, request.getTenantId());
                if (tags.size() != tagIds.size()) {
                    Set<UUID> foundIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
                    Set<UUID> invalidIds = tagIds.stream()
                            .filter(id -> !foundIds.contains(id))
                            .collect(Collectors.toSet());
                    throw new JournalBadRequestException("Tag IDs do not belong to this tenant or do not exist: " + invalidIds);
                }
                page.setTags(new HashSet<>(tags));
            }

            JournalPage saved = pageRepository.save(page);

            boolean hasTextContent = false;
            if (request.getBlocks() != null && !request.getBlocks().isEmpty()) {
                List<JournalBlock> blocks = new ArrayList<>();
                for (int i = 0; i < request.getBlocks().size(); i++) {
                    CreateBlockRequest blockReq = request.getBlocks().get(i);

                    Map<String, Object> safeContent = sanitizerUtil.sanitizeMap(blockReq.getContent());
                    Map<String, Object> safeMetadata = sanitizerUtil.sanitizeMap(blockReq.getMetadata());

                    JournalBlock block = JournalBlock.builder()
                            .tenantId(request.getTenantId())
                            .page(saved)
                            .type(blockReq.getType())
                            .orderIndex(blockReq.getOrderIndex() != null ? blockReq.getOrderIndex() : i)
                            .content(safeContent)
                            .metadata(safeMetadata)
                            .createdBy(userId.toString())
                            .createdAt(LocalDateTime.now())
                            .deleted(false)
                            .build();
                    blocks.add(block);

                    if (BlockTextUtil.hasText(safeContent)) {
                        hasTextContent = true;
                    }
                }
                List<JournalBlock> savedBlocks = blockRepository.saveAll(blocks);
                saved.setBlocks(savedBlocks);
            }

            if (hasTextContent) {
                eventPublisher.publishEvent(new PageAnalysisEvent(saved.getId()));
            }

            log.info("Journal page created: {} for user: {} on date: {}", saved.getId(), userId, saved.getEntryDate());
            return toDetailResponse(saved);
        } catch (TenantAccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating journal page for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to create journal page. Please try again.");
        }
    }

    @Override
    @Transactional(
            value = "journalTransactionManager",
            readOnly = true
    )
    public JournalPageDetailResponse getPage(UUID userId, UUID pageId) {
        try {
            JournalPage page = pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));
            return toDetailResponse(page);
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting journal page for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to get journal page. Please try again.");
        }
    }

    @Override
    @Transactional(
            value = "journalTransactionManager",
            readOnly = true
    )
    public Page<JournalPageResponse> getUserPages(UUID userId, Pageable pageable) {
        try {
            return pageRepository.findByUserIdAndDeletedFalse(userId, pageable)
                    .map(this::toListResponse);
        } catch (Exception e) {
            log.error("Error getting journal pages for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to get journal pages. Please try again.");
        }
    }

    @Override
    @Transactional(
            value = "journalTransactionManager",
            readOnly = true
    )
    public Page<JournalPageResponse> getUserPagesByDateRange(UUID userId, LocalDate from, LocalDate to, Pageable pageable) {
        try {
            return pageRepository.findByUserIdAndEntryDateBetweenAndDeletedFalse(userId, from, to, pageable)
                    .map(this::toListResponse);
        } catch (Exception e) {
            log.error("Error getting journal pages for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to get journal pages. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public JournalPageDetailResponse updatePage(UUID userId, UUID pageId, UpdateJournalPageRequest request) {
        try {
            JournalPage page = pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            if (request.getTitle() != null) page.setTitle(request.getTitle());
            if (request.getDescription() != null) page.setDescription(request.getDescription());
            if (request.getCoverImageUrl() != null) page.setCoverImageUrl(request.getCoverImageUrl());
            if (request.getIsPrivate() != null) page.setIsPrivate(request.getIsPrivate());

            page.setUpdatedBy(userId.toString());
            page.setUpdatedAt(LocalDateTime.now());

            JournalPage updated = pageRepository.save(page);
            log.info("Journal page updated: {} for user: {} on date: {}", updated.getId(), userId, updated.getEntryDate());
            return toDetailResponse(updated);
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating journal page for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to update journal page. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public void addTagToPage(UUID userId, UUID pageId, UUID tagId) {
        try {
            JournalPage page = pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            Tag tag = tagRepository.findByIdAndTenantIdAndDeletedFalse(tagId, page.getTenantId())
                    .orElseThrow(() -> new JournalNotFoundException("Tag not found or does not belong to this tenant: " + tagId));

            page.getTags().add(tag);
            pageRepository.save(page);
            log.info("Tag added to journal page: {} for user: {} on date: {}", tagId, userId, page.getEntryDate());
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding tag to journal page for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to add tag to journal page. Please try again.");
        }
    }

    @Transactional("journalTransactionManager")
    @Override
    public void removeTagFromPage(UUID userId, UUID pageId, UUID tagId) {
        try {
            JournalPage page = pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                    .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

            page.getTags().removeIf(tag -> tag.getId().equals(tagId));
            pageRepository.save(page);
            log.info("Tag removed from journal page: {} for user: {} on date: {}", tagId, userId, page.getEntryDate());
        } catch (JournalNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing tag from journal page for user: {} - {}", userId, e.getMessage(), e);
            throw new JournalException("Something went wrong. Failed to remove tag from journal page. Please try again.");
        }
    }

    @Override
    @Transactional("journalTransactionManager")
    public SentimentAnalysisResponse analyzePage(UUID userId, UUID pageId) {
        JournalPage page = pageRepository.findByIdAndUserIdAndDeletedFalse(pageId, userId)
                .orElseThrow(() -> new JournalNotFoundException("Journal page not found: " + pageId));

        String text = BlockTextUtil.aggregateText(page);
        if (text.isBlank()) {
            throw new JournalBadRequestException("Page has no text content to analyze.");
        }

        SentimentResponse mlResult = sentimentAnalyzerClient.analyze(new SentimentRequest(pageId.toString(), text));

        SentimentAnalysis saved = sentimentAnalysisService.saveAnalysisResult(
                page,
                mlResult.getSentimentLabel(),
                mlResult.getSentimentScores(),
                mlResult.getDominantEmotion(),
                mlResult.getEmotionVector(),
                mlResult.getAnalyzerVersion()
        );

        return SentimentAnalysisResponse.builder()
                .sentimentLabel(saved.getSentimentLabel())
                .sentimentScore(saved.getSentimentScore())
                .sentimentScores(saved.getSentimentScores())
                .dominantEmotion(saved.getDominantEmotion())
                .emotionVector(saved.getEmotionVector())
                .analysedAt(saved.getAnalysedAt())
                .build();
    }

    @Override
    @Transactional(value = "journalTransactionManager", readOnly = true)
    public List<JournalPageAnalyticsDto> getAnalyticsFeed(
            UUID userId, LocalDate from, LocalDate to) {
        return pageRepository.findAnalyticsFeed(userId, from, to);
    }

    private void requireMembership(UUID userId, UUID tenantId) {
        if (!tenantMembershipService.isMember(tenantId, userId)) {
            throw new TenantAccessDeniedException("Access denied: user is not a member of tenant " + tenantId);
        }
    }

    private JournalPageResponse toListResponse(JournalPage page) {
        return JournalPageResponse.builder()
                .id(page.getId())
                .tenantId(page.getTenantId())
                .userId(page.getUserId())
                .title(page.getTitle())
                .description(page.getDescription())
                .coverImageUrl(page.getCoverImageUrl())
                .entryDate(page.getEntryDate())
                .isPrivate(page.getIsPrivate())
                .tags(page.getTags().stream().map(this::toTagResponse).toList())
                .blockCount(page.getBlocks() != null ? (int) page.getBlocks().stream().filter(b -> !b.getDeleted()).count() : 0)
                .sentimentLabel(page.getSentimentAnalysis() != null ? page.getSentimentAnalysis().getSentimentLabel() : null)
                .sentimentScore(page.getSentimentAnalysis() != null ? page.getSentimentAnalysis().getSentimentScore() : null)
                .dominantEmotion(page.getSentimentAnalysis() != null ? page.getSentimentAnalysis().getDominantEmotion() : null)
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .build();
    }

    private JournalPageDetailResponse toDetailResponse(JournalPage page) {
        List<JournalBlockResponse> blockResponses = Collections.emptyList();
        if (page.getBlocks() != null) {
            blockResponses = page.getBlocks().stream()
                    .filter(b -> !b.getDeleted() && b.getParentBlock() == null)
                    .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                    .map(JournalBlockMapper::toResponse)
                    .toList();
        }

        AiEnrichmentResponse enrichmentResponse = null;
        if (page.getAiEnrichment() != null) {
            AiEnrichment e = page.getAiEnrichment();
            enrichmentResponse = AiEnrichmentResponse.builder()
                    .summary(e.getSummary())
                    .keywords(e.getKeywords())
                    .dominantEmotion(e.getDominantEmotion())
                    .recommendation(e.getRecommendation())
                    .generatedAt(e.getGeneratedAt())
                    .build();
        }

        SentimentAnalysisResponse sentimentResponse = null;
        if (page.getSentimentAnalysis() != null) {
            SentimentAnalysis sa = page.getSentimentAnalysis();
            sentimentResponse = SentimentAnalysisResponse.builder()
                    .sentimentLabel(sa.getSentimentLabel())
                    .sentimentScore(sa.getSentimentScore())
                    .sentimentScores(sa.getSentimentScores())
                    .dominantEmotion(sa.getDominantEmotion())
                    .emotionVector(sa.getEmotionVector())
                    .analysedAt(sa.getAnalysedAt())
                    .build();
        }

        return JournalPageDetailResponse.builder()
                .id(page.getId())
                .tenantId(page.getTenantId())
                .userId(page.getUserId())
                .title(page.getTitle())
                .description(page.getDescription())
                .coverImageUrl(page.getCoverImageUrl())
                .entryDate(page.getEntryDate())
                .isPrivate(page.getIsPrivate())
                .tags(page.getTags().stream().map(this::toTagResponse).toList())
                .blocks(blockResponses)
                .sentiment(sentimentResponse)
                .aiEnrichment(enrichmentResponse)
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .build();
    }

    private TagResponse toTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
