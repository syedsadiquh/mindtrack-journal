package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.user.dto.request.CreatePlanRequest;
import com.syedsadiquh.coreservice.user.dto.request.UpdatePlanRequest;
import com.syedsadiquh.coreservice.user.dto.response.PlanResponse;

import java.util.List;
import java.util.UUID;

public interface PlanAdminService {

    PlanResponse createPlan(CreatePlanRequest request);

    PlanResponse updatePlan(UUID planId, UpdatePlanRequest request);

    List<PlanResponse> getAllActivePlans();

    PlanResponse getPlan(UUID planId);

    void deactivatePlan(UUID planId);

    void reactivatePlan(UUID planId);
}
