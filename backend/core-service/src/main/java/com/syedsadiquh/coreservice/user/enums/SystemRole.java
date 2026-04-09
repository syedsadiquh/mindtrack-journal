package com.syedsadiquh.coreservice.user.enums;

/**
 * System-level roles managed by Keycloak (realm_access.roles in JWT).
 *
 * <p>These roles control what a user can do at the platform level,
 * independent of any tenant context.</p>
 *
 * <ul>
 *   <li><b>USER</b> — Can use the system: create journals, manage own profile.</li>
 *   <li><b>ADMIN</b> — Can manage users and take administrative actions across the platform.</li>
 *   <li><b>SYS_ADMIN</b> — Can change infrastructure-level settings: Keycloak config, plans, system toggles.</li>
 * </ul>
 *
 * <p><b>Note:</b> These values must match the realm role names configured in Keycloak.
 * Spring Security extracts them from the JWT and prefixes with {@code ROLE_},
 * so {@code @PreAuthorize("hasRole(T(...).ROLE_SYS_ADMIN)")} checks for the {@code SYS_ADMIN} realm role.</p>
 *
 * <p>Usage in {@code @PreAuthorize}:</p>
 * <pre>
 * &#64;PreAuthorize("hasRole(T(com.syedsadiquh.coreservice.user.enums.SystemRole).ROLE_SYS_ADMIN)")
 * </pre>
 * <p>Or use the shorthand constants:</p>
 * <pre>
 * &#64;PreAuthorize("hasRole('" + SystemRole.ROLE_SYS_ADMIN + "')")
 * </pre>
 */
public enum SystemRole {
    USER,
    ADMIN,
    SYS_ADMIN;

    // ─── Compile-time constants for @PreAuthorize annotations ───
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SYS_ADMIN = "SYS_ADMIN";

    // ─── SpEL-friendly "hasRole" expressions for @PreAuthorize ───
    public static final String HAS_ROLE_USER = "hasRole('" + ROLE_USER + "')";
    public static final String HAS_ROLE_ADMIN = "hasRole('" + ROLE_ADMIN + "')";
    public static final String HAS_ROLE_SYS_ADMIN = "hasRole('" + ROLE_SYS_ADMIN + "')";
    public static final String HAS_ROLE_ADMIN_OR_SYS_ADMIN = "hasRole('" + ROLE_ADMIN + "') or hasRole('" + ROLE_SYS_ADMIN + "')";
}
