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
@Table(name = "user_settings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserSetting extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "dark_mode")
    private Boolean darkMode;

    @Column(name = "email_notification")
    private Boolean emailNotification;

    private Boolean moodTracking = false;

    private Boolean aiReflection = false;

    // JSONB Mapping
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dashboard_layout", columnDefinition = "jsonb")
    private Map<String, Object> dashboardLayout;
}
