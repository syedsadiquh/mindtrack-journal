package com.syedsadiquh.coreservice.user.controller;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.CreatePlanRequest;
import com.syedsadiquh.coreservice.user.dto.request.UpdatePlanRequest;
import com.syedsadiquh.coreservice.user.dto.response.PlanResponse;
import com.syedsadiquh.coreservice.user.enums.SystemRole;
import com.syedsadiquh.coreservice.user.service.PlanAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin-only endpoints for managing subscription plans.
 *
 * <p><b>Zero-downtime updates</b>: Tenants hold a FK to the plan row.
 * Updating a plan's limits or features takes effect immediately for
 * all tenants on that plan — no migration, no restart.</p>
 *
 * <p><b>Safe deactivation</b>: Deactivating a plan prevents new signups
 * on it, but existing tenants keep their access until explicitly migrated.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class PlanAdminController {

    private final PlanAdminService planService;

    @PostMapping
    @PreAuthorize(SystemRole.HAS_ROLE_SYS_ADMIN)
    public ResponseEntity<BaseResponse<PlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {

        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Plan created", response));
    }

    @PutMapping("/{planId}")
    @PreAuthorize(SystemRole.HAS_ROLE_SYS_ADMIN)
    public ResponseEntity<BaseResponse<PlanResponse>> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody UpdatePlanRequest request) {

        PlanResponse response = planService.updatePlan(planId, request);
        return ResponseEntity.ok(new BaseResponse<>(true, "Plan updated", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<PlanResponse>>> getActivePlans() {
        List<PlanResponse> plans = planService.getAllActivePlans();
        return ResponseEntity.ok(new BaseResponse<>(true, "Plans retrieved", plans));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<BaseResponse<PlanResponse>> getPlan(@PathVariable UUID planId) {
        PlanResponse response = planService.getPlan(planId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Plan retrieved", response));
    }

    @PatchMapping("/{planId}/deactivate")
    @PreAuthorize(SystemRole.HAS_ROLE_SYS_ADMIN)
    public ResponseEntity<BaseResponse<Void>> deactivatePlan(@PathVariable UUID planId) {
        planService.deactivatePlan(planId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Plan deactivated"));
    }

    @PatchMapping("/{planId}/reactivate")
    @PreAuthorize(SystemRole.HAS_ROLE_SYS_ADMIN)
    public ResponseEntity<BaseResponse<Void>> reactivatePlan(@PathVariable UUID planId) {
        planService.reactivatePlan(planId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Plan reactivated"));
    }
}
