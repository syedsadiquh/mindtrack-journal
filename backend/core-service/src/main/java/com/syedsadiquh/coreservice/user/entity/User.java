package com.syedsadiquh.coreservice.user.entity;

import com.syedsadiquh.coreservice.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_first_name", columnList = "first_name"),
        @Index(name = "idx_user_last_name", columnList = "last_name"),
})
public class User extends AuditableEntity {

    /**
     * Primary key — sourced from Keycloak's 'sub' claim, NOT auto-generated.
     * This is why User extends AuditableEntity (no UUID v7) instead of BaseEntity.
     */
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_tenant_id", nullable = false)
    private Tenant defaultTenant;

    @Column(unique = true, length = 100)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "country_code", length = 5)
    private String countryCode;

    private String phone;

    @Column(length = 100)
    private String timezone;

    private String address;

    @Column(name = "language", nullable = false, length = 10)
    @Builder.Default
    private String language = "en";

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
