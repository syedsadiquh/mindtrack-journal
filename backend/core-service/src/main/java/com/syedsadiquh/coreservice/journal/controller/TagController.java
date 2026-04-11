package com.syedsadiquh.coreservice.journal.controller;

import com.syedsadiquh.coreservice.journal.dto.request.CreateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.request.UpdateTagRequest;
import com.syedsadiquh.coreservice.journal.dto.response.TagResponse;
import com.syedsadiquh.coreservice.journal.service.TagService;
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
@RequestMapping("/api/v1/journals/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<BaseResponse<TagResponse>> createTag(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTagRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        TagResponse response = tagService.createTag(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Tag created", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<TagResponse>>> getTags(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID tenantId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<TagResponse> tags = tagService.getTagsByTenant(userId, tenantId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Tags retrieved", tags));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<BaseResponse<TagResponse>> updateTag(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tagId,
            @RequestParam UUID tenantId,
            @Valid @RequestBody UpdateTagRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        TagResponse response = tagService.updateTag(userId, tagId, tenantId, request);
        return ResponseEntity.ok(new BaseResponse<>(true, "Tag updated", response));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<BaseResponse<Void>> deleteTag(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tagId,
            @RequestParam UUID tenantId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        tagService.deleteTag(userId, tagId, tenantId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Tag deleted"));
    }
}
