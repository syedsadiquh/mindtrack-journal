package com.syedsadiquh.coreservice.user.entity;

import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import com.syedsadiquh.coreservice.user.enums.PlanTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Defines the subscription plan a tenant is on.
 *
 * <p>Each plan has a tier (FREE / PREMIUM / ENTERPRISE) with associated
 * feature limits, quotas, and pricing. Tenants reference a plan via FK,
 * allowing plan changes without altering tenant rows.</p>
 *
 * <p>Seeded at startup with three default plans. The payment service
 * handles upgrades/downgrades by updating the tenant's plan FK.</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "plans", indexes = {
        @Index(name = "idx_plan_tier", columnList = "tier")
})
public class Plan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private PlanTier tier;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ── Feature limits ──────────────────────────────────────────────

    /** Max journal pages a tenant can create. -1 = unlimited. */
    @Column(name = "max_pages")
    @Builder.Default
    private Integer maxPages = -1;

    /** Max blocks per page. -1 = unlimited. */
    @Column(name = "max_blocks_per_page")
    @Builder.Default
    private Integer maxBlocksPerPage = -1;

    /** Max tags a tenant can create. -1 = unlimited. */
    @Column(name = "max_tags")
    @Builder.Default
    private Integer maxTags = -1;

    /** Max members per tenant. -1 = unlimited. */
    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 1;

    @Column(name = "ai_enrichment_enabled")
    @Builder.Default
    private Boolean aiEnrichmentEnabled = false;

    @Column(name = "sentiment_analysis_enabled")
    @Builder.Default
    private Boolean sentimentAnalysisEnabled = true;

    @Column(name = "sharing_enabled")
    @Builder.Default
    private Boolean sharingEnabled = false;

    @Column(name = "export_enabled")
    @Builder.Default
    private Boolean exportEnabled = false;

    // ── Pricing ─────────────────────────────────────────────────────

    /** Monthly price in the smallest currency unit (e.g. cents). 0 = free. */
    @Column(name = "price_monthly", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceMonthly = BigDecimal.ZERO;

    /** Yearly price. 0 = free. */
    @Column(name = "price_yearly", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceYearly = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}
