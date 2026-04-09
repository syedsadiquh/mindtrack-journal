package com.syedsadiquh.coreservice.user.enums;

/**
 * Tenant-level roles stored in the {@code tenant_members} table.
 *
 * <p>These roles control what a user can do within a specific tenant/workspace.</p>
 *
 * <ul>
 *   <li><b>MEMBER</b> — Can use the journal system within the workspace: create/edit own entries.</li>
 *   <li><b>ADMIN</b> — Everything a MEMBER can do, plus manage the workspace and its members
 *       (invite/remove members, change workspace settings).</li>
 *   <li><b>OWNER</b> — Everything an ADMIN can do, plus tenant-level destructive actions
 *       (delete workspace, transfer ownership, change tenant plan).</li>
 * </ul>
 */
public enum TenantRole {
    MEMBER,
    ADMIN,
    OWNER
}
