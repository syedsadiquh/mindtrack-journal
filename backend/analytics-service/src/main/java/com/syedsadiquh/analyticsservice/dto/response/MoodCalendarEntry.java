package com.syedsadiquh.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoodCalendarEntry {
    private String date;
    private Integer entryCount;
    private Double sentimentScore;
    private String sentimentLabel;
    private String dominantEmotion;
}
