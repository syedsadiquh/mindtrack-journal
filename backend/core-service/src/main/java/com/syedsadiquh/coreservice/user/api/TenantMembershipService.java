package com.syedsadiquh.coreservice.user.api;

import java.util.UUID;

/**
 * Public API of the user module for cross-module tenant membership checks.
 */
public interface TenantMembershipService {

    boolean isMember(UUID tenantId, UUID userId);
}
