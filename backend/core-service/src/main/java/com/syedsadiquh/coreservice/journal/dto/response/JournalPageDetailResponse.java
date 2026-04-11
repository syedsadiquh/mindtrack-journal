package com.syedsadiquh.coreservice.journal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalPageDetailResponse {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String title;
    private String description;
    private String coverImageUrl;
    private LocalDate entryDate;
    private Boolean isPrivate;
    private List<TagResponse> tags;
    private List<JournalBlockResponse> blocks;
    private AiEnrichmentResponse aiEnrichment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
