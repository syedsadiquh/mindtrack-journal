package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateJournalPageRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateJournalPageRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalPageAnalyticsDto;
import com.syedsadiquh.coreservice.journal.dto.response.JournalPageDetailResponse;
import com.syedsadiquh.coreservice.journal.dto.response.JournalPageResponse;
import com.syedsadiquh.coreservice.journal.dto.response.SentimentAnalysisResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JournalPageService {

    JournalPageDetailResponse createPage(UUID userId, CreateJournalPageRequest request);

    JournalPageDetailResponse getPage(UUID userId, UUID pageId);

    Page<JournalPageResponse> getUserPages(UUID userId, Pageable pageable);

    Page<JournalPageResponse> getUserPagesByDateRange(UUID userId, LocalDate from, LocalDate to, Pageable pageable);

    JournalPageDetailResponse updatePage(UUID userId, UUID pageId, UpdateJournalPageRequest request);

    void addTagToPage(UUID userId, UUID pageId, UUID tagId);

    void removeTagFromPage(UUID userId, UUID pageId, UUID tagId);

    SentimentAnalysisResponse analyzePage(UUID userId, UUID pageId);

    List<JournalPageAnalyticsDto> getAnalyticsFeed(UUID userId, LocalDate from, LocalDate to);
}
