package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.dto.request.CreateJournalEntryRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateJournalEntryRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JournalService {

    JournalEntryResponse createEntry(UUID userId, CreateJournalEntryRequest request);

    JournalEntryResponse getEntry(UUID userId, UUID entryId);

    Page<JournalEntryResponse> getUserEntries(UUID userId, Pageable pageable);

    JournalEntryResponse updateEntry(UUID userId, UUID entryId, UpdateJournalEntryRequest request);

    void deleteEntry(UUID userId, UUID entryId);
}

