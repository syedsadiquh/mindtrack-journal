package com.syedsadiquh.userservice.entity.compositeKey;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantMemberId implements Serializable {
    private UUID tenantId;
    private UUID userId;
}
