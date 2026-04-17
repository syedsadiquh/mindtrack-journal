package com.syedsadiquh.coreservice.journal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalPageAnalyticsDto {
    private LocalDate entryDate;
    private String sentimentLabel;
    private Float sentimentScore;
    private String dominantEmotion;
}
