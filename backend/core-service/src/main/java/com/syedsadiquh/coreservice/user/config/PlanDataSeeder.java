package com.syedsadiquh.coreservice.user.config;

import com.syedsadiquh.coreservice.user.entity.Plan;
import com.syedsadiquh.coreservice.user.enums.PlanTier;
import com.syedsadiquh.coreservice.user.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds the default subscription plans on startup if they don't already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanDataSeeder implements ApplicationRunner {

    private final PlanRepository planRepository;

    @Override
    @Transactional("userTransactionManager")
    public void run(ApplicationArguments args) {
        seedIfAbsent(Plan.builder()
                .tier(PlanTier.FREE)
                .displayName("Free")
                .description("Basic journaling for individuals")
                .maxPages(-1)
                .maxBlocksPerPage(50)
                .maxTags(10)
                .maxMembers(1)
                .aiEnrichmentEnabled(false)
                .sentimentAnalysisEnabled(true)
                .sharingEnabled(false)
                .exportEnabled(false)
                .priceMonthly(BigDecimal.ZERO)
                .priceYearly(BigDecimal.ZERO)
                .currency("INR")
                .active(true)
                .deleted(false)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build());

        seedIfAbsent(Plan.builder()
                .tier(PlanTier.PREMIUM)
                .displayName("Premium")
                .description("Advanced journaling with AI insights and sharing")
                .maxPages(-1)
                .maxBlocksPerPage(-1)
                .maxTags(-1)
                .maxMembers(5)
                .aiEnrichmentEnabled(true)
                .sentimentAnalysisEnabled(true)
                .sharingEnabled(true)
                .exportEnabled(true)
                .priceMonthly(new BigDecimal("199"))
                .priceYearly(new BigDecimal("1999"))
                .currency("INR")
                .active(true)
                .deleted(false)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build());

        seedIfAbsent(Plan.builder()
                .tier(PlanTier.ENTERPRISE)
                .displayName("Enterprise")
                .description("Full platform for clinics, therapists, and organisations")
                .maxPages(-1)
                .maxBlocksPerPage(-1)
                .maxTags(-1)
                .maxMembers(-1)
                .aiEnrichmentEnabled(true)
                .sentimentAnalysisEnabled(true)
                .sharingEnabled(true)
                .exportEnabled(true)
                .priceMonthly(new BigDecimal("899"))
                .priceYearly(new BigDecimal("8999"))
                .currency("INR")
                .active(true)
                .deleted(false)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build());
    }

    private void seedIfAbsent(Plan plan) {
        if (!planRepository.existsByTier(plan.getTier())) {
            planRepository.save(plan);
            log.info("Seeded plan: {}", plan.getTier());
        }
    }
}
