package com.syedsadiquh.analyticsservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

/**
 * Minimal projection of core-service's JournalPageResponse.
 * Only fields needed for analytics computation are mapped.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalPageDto {
    private LocalDate entryDate;
    private String sentimentLabel;
    private Float sentimentScore;
    private String dominantEmotion;
}
