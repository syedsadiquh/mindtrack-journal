package com.syedsadiquh.coreservice.user.dto.request;

import com.syedsadiquh.coreservice.user.enums.PlanTier;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanRequest {

    @NotNull
    private PlanTier tier;

    @NotEmpty
    private String displayName;

    private String description;

    private Integer maxPages = -1;
    private Integer maxBlocksPerPage = -1;
    private Integer maxTags = -1;
    private Integer maxMembers = 1;

    private Boolean aiEnrichmentEnabled = false;
    private Boolean sentimentAnalysisEnabled = true;
    private Boolean sharingEnabled = false;
    private Boolean exportEnabled = false;

    private BigDecimal priceMonthly = BigDecimal.ZERO;
    private BigDecimal priceYearly = BigDecimal.ZERO;
    private String currency = "USD";
}
