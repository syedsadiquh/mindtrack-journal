package com.syedsadiquh.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmotionDistributionItem {
    private String emotion;
    private Long count;
    private Double percentage;
}
