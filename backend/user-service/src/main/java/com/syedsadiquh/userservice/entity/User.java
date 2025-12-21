package com.syedsadiquh.userservice.entity;

import com.syedsadiquh.userservice.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
        @Index(name = "idx_user_email", columnList = "email")
})
public class User extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_tenant_id", nullable = false)
    private Tenant defaultTenant;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String countryCode;

    private String phone;

    private String timezone;

    private String address;

    @Column(nullable = false)
    private Boolean active = true;

}
