package com.syedsadiquh.coreservice.journal.controller;

import com.syedsadiquh.coreservice.journal.dto.request.CreateJournalEntryRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateJournalEntryRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalEntryResponse;
import com.syedsadiquh.coreservice.journal.service.JournalService;
import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Journal REST controller.
 * Extracts userId directly from the JWT security context — zero coupling to the user module.
 */
@RestController
@RequestMapping("/api/v1/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    public ResponseEntity<BaseResponse<JournalEntryResponse>> createEntry(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateJournalEntryRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalEntryResponse response = journalService.createEntry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Journal entry created", response));
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<BaseResponse<JournalEntryResponse>> getEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID entryId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalEntryResponse response = journalService.getEntry(userId, entryId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Journal entry retrieved", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<JournalEntryResponse>>> getUserEntries(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        Page<JournalEntryResponse> page = journalService.getUserEntries(userId, pageable);
        return ResponseEntity.ok(new BaseResponse<>(true, "Journal entries retrieved", page));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<BaseResponse<JournalEntryResponse>> updateEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID entryId,
            @Valid @RequestBody UpdateJournalEntryRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalEntryResponse response = journalService.updateEntry(userId, entryId, request);
        return ResponseEntity.ok(new BaseResponse<>(true, "Journal entry updated", response));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<BaseResponse<Void>> deleteEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID entryId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        journalService.deleteEntry(userId, entryId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Journal entry deleted"));
    }
}

