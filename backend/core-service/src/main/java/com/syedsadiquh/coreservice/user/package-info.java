/**
 * User module — bounded context for authentication, user management, and tenancy.
 * Exposes only the public API surface via this top-level package.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "infrastructure"}
)
package com.syedsadiquh.coreservice.user;

