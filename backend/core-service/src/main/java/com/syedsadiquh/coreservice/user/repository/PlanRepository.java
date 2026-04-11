package com.syedsadiquh.coreservice.user.repository;

import com.syedsadiquh.coreservice.user.entity.Plan;
import com.syedsadiquh.coreservice.user.enums.PlanTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByTier(PlanTier tier);

    boolean existsByTier(PlanTier tier);
}
