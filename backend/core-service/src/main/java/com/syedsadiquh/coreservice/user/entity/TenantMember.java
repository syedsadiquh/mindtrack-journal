package com.syedsadiquh.coreservice.user.entity;

import com.syedsadiquh.coreservice.shared.entity.AuditableEntity;
import com.syedsadiquh.coreservice.user.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tenant_members")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMember extends AuditableEntity {

    @EmbeddedId
    private TenantMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tenantId")
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;
}

