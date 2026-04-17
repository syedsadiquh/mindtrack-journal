package com.syedsadiquh.analyticsservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "daily_sentiment_cache",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "entry_date"}),
        indexes = @Index(name = "idx_daily_sentiment_user_date", columnList = "user_id, entry_date")
)
public class DailySentimentCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "entry_count")
    @Builder.Default
    private Integer entryCount = 1;

    @Column(name = "avg_sentiment_score")
    private Double avgSentimentScore;

    @Column(name = "dominant_label", length = 20)
    private String dominantLabel;

    @Column(name = "dominant_emotion", length = 50)
    private String dominantEmotion;
}
