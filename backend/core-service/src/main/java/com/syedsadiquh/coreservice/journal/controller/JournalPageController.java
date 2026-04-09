package com.syedsadiquh.coreservice.journal.controller;

import com.syedsadiquh.coreservice.journal.dto.request.CreateJournalPageRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateJournalPageRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalPageDetailResponse;
import com.syedsadiquh.coreservice.journal.dto.response.JournalPageResponse;
import com.syedsadiquh.coreservice.journal.service.JournalPageService;
import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journals/pages")
@RequiredArgsConstructor
public class JournalPageController {

    private final JournalPageService journalPageService;

    @PostMapping
    public ResponseEntity<BaseResponse<JournalPageDetailResponse>> createPage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateJournalPageRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalPageDetailResponse response = journalPageService.createPage(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Journal page created", response));
    }

    @GetMapping("/{pageId}")
    public ResponseEntity<BaseResponse<JournalPageDetailResponse>> getPage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalPageDetailResponse response = journalPageService.getPage(userId, pageId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Journal page retrieved", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<JournalPageResponse>>> getUserPages(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "entryDate") Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());

        Page<JournalPageResponse> page;
        if (from != null && to != null) {
            page = journalPageService.getUserPagesByDateRange(userId, from, to, pageable);
        } else {
            page = journalPageService.getUserPages(userId, pageable);
        }

        return ResponseEntity.ok(new BaseResponse<>(true, "Journal pages retrieved", page));
    }

    @PutMapping("/{pageId}")
    public ResponseEntity<BaseResponse<JournalPageDetailResponse>> updatePage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @Valid @RequestBody UpdateJournalPageRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalPageDetailResponse response = journalPageService.updatePage(userId, pageId, request);
        return ResponseEntity.ok(new BaseResponse<>(true, "Journal page updated", response));
    }

    @PostMapping("/{pageId}/tags/{tagId}")
    public ResponseEntity<BaseResponse<Void>> addTagToPage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @PathVariable UUID tagId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        journalPageService.addTagToPage(userId, pageId, tagId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Tag added to page"));
    }

    @DeleteMapping("/{pageId}/tags/{tagId}")
    public ResponseEntity<BaseResponse<Void>> removeTagFromPage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @PathVariable UUID tagId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        journalPageService.removeTagFromPage(userId, pageId, tagId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Tag removed from page"));
    }
}
