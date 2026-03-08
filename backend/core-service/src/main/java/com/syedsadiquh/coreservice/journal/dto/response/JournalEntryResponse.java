package com.syedsadiquh.coreservice.journal.dto.response;

import com.syedsadiquh.coreservice.journal.enums.Mood;
import com.syedsadiquh.coreservice.journal.enums.SentimentLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryResponse {
    private UUID id;
    private UUID userId;
    private UUID tenantId;
    private String title;
    private String content;
    private List<String> tags;
    private Mood mood;
    private Double sentimentScore;
    private SentimentLabel sentimentLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

