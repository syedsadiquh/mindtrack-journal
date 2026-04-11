package com.syedsadiquh.coreservice.user.entity;

import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_settings", indexes = {
        @Index(name = "idx_setting_user_id", columnList = "user_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserSetting extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "dark_mode")
    @Builder.Default
    private Boolean darkMode = false;

    @Column(name = "email_notification")
    @Builder.Default
    private Boolean emailNotification = true;

    @Column(name = "mood_tracking_opt_in")
    @Builder.Default
    private Boolean moodTracking = true;

    @Column(name = "ai_reflection_opt_in")
    @Builder.Default
    private Boolean aiReflection = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dashboard_layout", columnDefinition = "jsonb")
    private Map<String, Object> dashboardLayout;
}
