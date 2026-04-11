package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.user.dto.request.CreatePlanRequest;
import com.syedsadiquh.coreservice.user.dto.request.UpdatePlanRequest;
import com.syedsadiquh.coreservice.user.dto.response.PlanResponse;
import com.syedsadiquh.coreservice.user.entity.Plan;
import com.syedsadiquh.coreservice.user.exception.UserException;
import com.syedsadiquh.coreservice.user.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanAdminServiceImpl implements PlanAdminService {

    private final PlanRepository planRepository;

    @Transactional("userTransactionManager")
    @Override
    public PlanResponse createPlan(CreatePlanRequest request) {
        if (planRepository.existsByTier(request.getTier())) {
            throw new UserException("Plan with tier '" + request.getTier() + "' already exists. " +
                    "Use the update endpoint to modify it.");
        }

        Plan plan = Plan.builder()
                .tier(request.getTier())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .maxPages(request.getMaxPages())
                .maxBlocksPerPage(request.getMaxBlocksPerPage())
                .maxTags(request.getMaxTags())
                .maxMembers(request.getMaxMembers())
                .aiEnrichmentEnabled(request.getAiEnrichmentEnabled())
                .sentimentAnalysisEnabled(request.getSentimentAnalysisEnabled())
                .sharingEnabled(request.getSharingEnabled())
                .exportEnabled(request.getExportEnabled())
                .priceMonthly(request.getPriceMonthly())
                .priceYearly(request.getPriceYearly())
                .currency(request.getCurrency())
                .active(true)
                .deleted(false)
                .createdBy("ADMIN")
                .createdAt(LocalDateTime.now())
                .build();

        Plan saved = planRepository.save(plan);
        log.info("Plan created: {} ({})", saved.getDisplayName(), saved.getTier());
        return toResponse(saved);
    }

    /**
     * Updates an existing plan in-place. All tenants currently on this plan
     * will immediately see the new limits and features — zero downtime,
     * no migration needed.
     *
     * <p><b>Safe to call live</b> because:</p>
     * <ul>
     *   <li>Tenants hold a FK to the plan row; the FK doesn't change.</li>
     *   <li>Limit checks happen at request time, so new limits apply instantly.</li>
     *   <li>No cached plan copies — services always read from DB.</li>
     * </ul>
     */
    @Transactional("userTransactionManager")
    @Override
    public PlanResponse updatePlan(UUID planId, UpdatePlanRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new UserException("Plan not found: " + planId));

        if (request.getDisplayName() != null) plan.setDisplayName(request.getDisplayName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getMaxPages() != null) plan.setMaxPages(request.getMaxPages());
        if (request.getMaxBlocksPerPage() != null) plan.setMaxBlocksPerPage(request.getMaxBlocksPerPage());
        if (request.getMaxTags() != null) plan.setMaxTags(request.getMaxTags());
        if (request.getMaxMembers() != null) plan.setMaxMembers(request.getMaxMembers());
        if (request.getAiEnrichmentEnabled() != null) plan.setAiEnrichmentEnabled(request.getAiEnrichmentEnabled());
        if (request.getSentimentAnalysisEnabled() != null) plan.setSentimentAnalysisEnabled(request.getSentimentAnalysisEnabled());
        if (request.getSharingEnabled() != null) plan.setSharingEnabled(request.getSharingEnabled());
        if (request.getExportEnabled() != null) plan.setExportEnabled(request.getExportEnabled());
        if (request.getPriceMonthly() != null) plan.setPriceMonthly(request.getPriceMonthly());
        if (request.getPriceYearly() != null) plan.setPriceYearly(request.getPriceYearly());
        if (request.getCurrency() != null) plan.setCurrency(request.getCurrency());

        plan.setUpdatedBy("ADMIN");
        plan.setUpdatedAt(LocalDateTime.now());

        Plan updated = planRepository.save(plan);
        log.info("Plan updated: {} ({})", updated.getDisplayName(), updated.getTier());
        return toResponse(updated);
    }

    @Override
    public List<PlanResponse> getAllActivePlans() {
        return planRepository.findAll().stream()
                .filter(p -> p.getActive() && !p.getDeleted())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PlanResponse getPlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new UserException("Plan not found: " + planId));
        return toResponse(plan);
    }

    /**
     * Deactivates a plan so no new tenants can be assigned to it.
     * Existing tenants on this plan are NOT affected — they keep their
     * current limits until explicitly migrated.
     */
    @Transactional("userTransactionManager")
    @Override
    public void deactivatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new UserException("Plan not found: " + planId));
        plan.setActive(false);
        plan.setUpdatedBy("ADMIN");
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);
        log.info("Plan deactivated: {} ({})", plan.getDisplayName(), plan.getTier());
    }

    @Transactional("userTransactionManager")
    @Override
    public void reactivatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new UserException("Plan not found: " + planId));
        plan.setActive(true);
        plan.setUpdatedBy("ADMIN");
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);
        log.info("Plan reactivated: {} ({})", plan.getDisplayName(), plan.getTier());
    }

    private PlanResponse toResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .tier(plan.getTier())
                .displayName(plan.getDisplayName())
                .description(plan.getDescription())
                .maxPages(plan.getMaxPages())
                .maxBlocksPerPage(plan.getMaxBlocksPerPage())
                .maxTags(plan.getMaxTags())
                .maxMembers(plan.getMaxMembers())
                .aiEnrichmentEnabled(plan.getAiEnrichmentEnabled())
                .sentimentAnalysisEnabled(plan.getSentimentAnalysisEnabled())
                .sharingEnabled(plan.getSharingEnabled())
                .exportEnabled(plan.getExportEnabled())
                .priceMonthly(plan.getPriceMonthly())
                .priceYearly(plan.getPriceYearly())
                .currency(plan.getCurrency())
                .active(plan.getActive())
                .build();
    }
}
