package com.syedsadiquh.coreservice.journal.entity;

import com.syedsadiquh.coreservice.journal.enums.PermissionRole;
import com.syedsadiquh.coreservice.journal.enums.PermissionTargetType;
import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Polymorphic permission model for sharing journal pages or individual blocks.
 *
 * <p><b>Not implemented in v1</b> — entity exists only to establish the schema
 * for future collaboration features (doctor-patient sharing, parent-child
 * monitoring, peer sharing).</p>
 *
 * <p>Usage examples (future):
 * <ul>
 *   <li>A patient shares a journal page with their therapist as VIEWER</li>
 *   <li>A parent is granted VIEWER access to a child's mood blocks</li>
 *   <li>A user grants EDITOR access to a co-journaling partner</li>
 * </ul>
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permissions_user_id", columnList = "user_id"),
        @Index(name = "idx_permissions_target", columnList = "targetType, target_id")
})
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private PermissionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    /**
     * The user being granted access.
     * References users(id) in the user DB — no cross-schema FK.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionRole role;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    /**
     * The user who granted the permission.
     * References users(id) in the user DB — no cross-schema FK.
     */
    @Column(name = "granted_by", nullable = false)
    private UUID grantedBy;
}
