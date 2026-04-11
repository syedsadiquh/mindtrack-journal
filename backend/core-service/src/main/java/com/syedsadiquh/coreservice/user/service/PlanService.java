package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.user.dto.response.PlanResponse;

import java.util.List;
import java.util.UUID;

public interface PlanService {

    List<PlanResponse> getAllActivePlans();

    PlanResponse getPlan(UUID planId);
}
