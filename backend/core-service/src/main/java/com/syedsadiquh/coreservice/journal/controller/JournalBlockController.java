package com.syedsadiquh.coreservice.journal.controller;

import com.syedsadiquh.coreservice.journal.dto.request.CreateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.request.ReorderBlocksRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateBlockRequest;
import com.syedsadiquh.coreservice.journal.dto.response.JournalBlockResponse;
import com.syedsadiquh.coreservice.journal.service.JournalBlockService;
import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journals/pages/{pageId}/blocks")
@RequiredArgsConstructor
public class JournalBlockController {

    private final JournalBlockService journalBlockService;

    @PostMapping
    public ResponseEntity<BaseResponse<JournalBlockResponse>> addBlock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @Valid @RequestBody CreateBlockRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalBlockResponse response = journalBlockService.addBlock(userId, pageId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Block added", response));
    }

    @PutMapping("/{blockId}")
    public ResponseEntity<BaseResponse<JournalBlockResponse>> updateBlock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @PathVariable UUID blockId,
            @Valid @RequestBody UpdateBlockRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        JournalBlockResponse response = journalBlockService.updateBlock(userId, pageId, blockId, request);
        return ResponseEntity.ok(new BaseResponse<>(true, "Block updated", response));
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<BaseResponse<Void>> deleteBlock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @PathVariable UUID blockId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        journalBlockService.softDeleteBlock(userId, pageId, blockId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Block deleted"));
    }

    @PutMapping("/reorder")
    public ResponseEntity<BaseResponse<List<JournalBlockResponse>>> reorderBlocks(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pageId,
            @Valid @RequestBody ReorderBlocksRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        List<JournalBlockResponse> response = journalBlockService.reorderBlocks(userId, pageId, request);
        return ResponseEntity.ok(new BaseResponse<>(true, "Blocks reordered", response));
    }
}
