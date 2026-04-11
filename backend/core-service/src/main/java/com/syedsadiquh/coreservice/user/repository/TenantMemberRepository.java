package com.syedsadiquh.coreservice.user.repository;

import com.syedsadiquh.coreservice.user.entity.TenantMember;
import com.syedsadiquh.coreservice.user.entity.TenantMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TenantMemberRepository extends JpaRepository<TenantMember, TenantMemberId> {
    List<TenantMember> findByUserId(UUID userId);

    boolean existsByIdTenantIdAndIdUserId(UUID tenantId, UUID userId);
}

