package com.syedsadiquh.coreservice.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanRequest {

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
}
