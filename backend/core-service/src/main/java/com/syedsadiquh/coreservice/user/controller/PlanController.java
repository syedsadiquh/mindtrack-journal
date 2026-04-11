package com.syedsadiquh.coreservice.user.controller;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.response.PlanResponse;
import com.syedsadiquh.coreservice.user.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Public endpoints for viewing subscription plans.
 */
@RestController
@RequestMapping( "/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

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
}
