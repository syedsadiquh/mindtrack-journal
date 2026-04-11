/**
 * User module — bounded context for authentication, user management, and tenancy.
 * Exposes the public API surface via this top-level package.
 * OR defines the internal API surface via the {@code api} subpackage.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "infrastructure"}
)
package com.syedsadiquh.coreservice.user;

