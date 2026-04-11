package com.syedsadiquh.coreservice.user.dto.response;

import com.syedsadiquh.coreservice.user.enums.PlanTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private UUID id;
    private PlanTier tier;
    private String displayName;
    private String description;

    private Integer maxPages;
    private Integer maxBlocksPerPage;
    private Integer maxTags;
    private Integer maxMembers;

    private Boolean aiEnrichmentEnabled;
    private Boolean sentimentAnalysisEnabled;
    private Boolean sharingEnabled;
    private Boolean exportEnabled;

    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private String currency;
    private Boolean active;
}
