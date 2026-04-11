package com.syedsadiquh.coreservice.journal.dto.response;

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
public class JournalPageResponse {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String title;
    private String description;
    private String coverImageUrl;
    private LocalDate entryDate;
    private Boolean isPrivate;
    private List<TagResponse> tags;
    private int blockCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
