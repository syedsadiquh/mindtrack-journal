package com.syedsadiquh.coreservice.user.entity;

import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_user_id", columnList = "user_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Keycloak session ID — used to correlate with Keycloak's session lifecycle
     * for invalidation and single-sign-out support.
     */
    @Column(name = "keycloak_session_id")
    private UUID keycloakSessionId;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_info", columnDefinition = "jsonb")
    private Map<String, Object> deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column()
    private String location;
}
