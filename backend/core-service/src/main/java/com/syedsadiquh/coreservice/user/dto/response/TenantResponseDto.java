package com.syedsadiquh.coreservice.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.syedsadiquh.coreservice.user.enums.PlanTier;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantResponseDto {
    private UUID tenantId;
    private String tenantName;
    private PlanTier planTier;
    private String tenantSlug;
    private Boolean active;
}
