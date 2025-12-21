package com.syedsadiquh.userservice.entity;

import com.syedsadiquh.userservice.entity.base.BaseEntity;
import com.syedsadiquh.userservice.enums.PlanTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_slug", columnList = "slug")
})
public class Tenant extends BaseEntity {
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(name = "plan_id", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PlanTier planTier;

    private Boolean active = true;
}
