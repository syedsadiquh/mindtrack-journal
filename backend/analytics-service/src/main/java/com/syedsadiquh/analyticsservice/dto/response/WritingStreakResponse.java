package com.syedsadiquh.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WritingStreakResponse {
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastEntryDate;
}
