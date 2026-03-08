package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateJournalEntryRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateJournalEntryRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalEntryResponse;
import com.syedsadiquh.coreservice.journal.entity.JournalEntry;
import com.syedsadiquh.coreservice.journal.event.JournalEntryCreatedEvent;
import com.syedsadiquh.coreservice.journal.exception.JournalException;
import com.syedsadiquh.coreservice.journal.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public JournalEntryResponse createEntry(UUID userId, CreateJournalEntryRequest request) {
        JournalEntry entry = JournalEntry.builder()
                .userId(userId)
                .tenantId(request.getTenantId())
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .mood(request.getMood())
                .createdBy(userId.toString())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        JournalEntry saved = journalEntryRepository.save(entry);

        // Publish event AFTER transaction commits — consumed asynchronously
        // by SentimentAnalysisListener to call the ML service via Feign.
        // This ensures the main HTTP thread returns 201 immediately.
        eventPublisher.publishEvent(new JournalEntryCreatedEvent(saved.getId(), saved.getContent()));

        log.info("Journal entry created: {} for user: {}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Override
    public JournalEntryResponse getEntry(UUID userId, UUID entryId) {
        JournalEntry entry = journalEntryRepository.findByIdAndUserIdAndDeletedFalse(entryId, userId)
                .orElseThrow(() -> new JournalException("Journal entry not found: " + entryId));
        return toResponse(entry);
    }

    @Override
    public Page<JournalEntryResponse> getUserEntries(UUID userId, Pageable pageable) {
        return journalEntryRepository.findByUserIdAndDeletedFalse(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    @Override
    public JournalEntryResponse updateEntry(UUID userId, UUID entryId, UpdateJournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findByIdAndUserIdAndDeletedFalse(entryId, userId)
                .orElseThrow(() -> new JournalException("Journal entry not found: " + entryId));

        if (request.getTitle() != null) entry.setTitle(request.getTitle());
        if (request.getContent() != null) {
            entry.setContent(request.getContent());
            // Re-trigger sentiment analysis for updated content
            eventPublisher.publishEvent(new JournalEntryCreatedEvent(entry.getId(), request.getContent()));
        }
        if (request.getTags() != null) entry.setTags(request.getTags());
        if (request.getMood() != null) entry.setMood(request.getMood());

        entry.setUpdatedBy(userId.toString());
        entry.setUpdatedAt(LocalDateTime.now());

        JournalEntry updated = journalEntryRepository.save(entry);
        return toResponse(updated);
    }

    @Transactional
    @Override
    public void deleteEntry(UUID userId, UUID entryId) {
        JournalEntry entry = journalEntryRepository.findByIdAndUserIdAndDeletedFalse(entryId, userId)
                .orElseThrow(() -> new JournalException("Journal entry not found: " + entryId));

        // Soft delete
        entry.setDeleted(true);
        entry.setDeletedBy(userId.toString());
        entry.setDeletedAt(LocalDateTime.now());
        journalEntryRepository.save(entry);

        log.info("Journal entry soft-deleted: {} by user: {}", entryId, userId);
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {
        return JournalEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .tenantId(entry.getTenantId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .tags(entry.getTags())
                .mood(entry.getMood())
                .sentimentScore(entry.getSentimentScore())
                .sentimentLabel(entry.getSentimentLabel())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}

