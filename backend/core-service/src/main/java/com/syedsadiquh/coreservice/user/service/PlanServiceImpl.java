package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.user.dto.response.PlanResponse;
import com.syedsadiquh.coreservice.user.entity.Plan;
import com.syedsadiquh.coreservice.user.exception.UserException;
import com.syedsadiquh.coreservice.user.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

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
