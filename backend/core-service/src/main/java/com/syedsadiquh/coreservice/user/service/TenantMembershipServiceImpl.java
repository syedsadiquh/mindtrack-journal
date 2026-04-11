package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.user.api.TenantMembershipService;
import com.syedsadiquh.coreservice.user.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantMembershipServiceImpl implements TenantMembershipService {

    private final TenantMemberRepository tenantMemberRepository;

    @Override
    public boolean isMember(UUID tenantId, UUID userId) {
        return tenantMemberRepository.existsByIdTenantIdAndIdUserId(tenantId, userId);
    }
}
