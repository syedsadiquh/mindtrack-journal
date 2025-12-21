package com.syedsadiquh.userservice.entity;

import com.syedsadiquh.userservice.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_info", columnDefinition = "jsonb")
    private Map<String, Object> deviceInfo;

    private String ipAddress;

    private String location;
}
