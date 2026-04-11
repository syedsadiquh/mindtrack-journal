package com.syedsadiquh.coreservice.journal.entity;

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

import java.time.LocalDateTime;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "sentiment_analysis", indexes = {
        @Index(name = "idx_sentiment_block_id", columnList = "block_id")
})
public class SentimentAnalysis extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false, unique = true)
    private JournalBlock block;

    @Column(name = "analysed_at")
    private LocalDateTime analysedAt;

    /**
     * Sentiment classification: POSITIVE, NEUTRAL, NEGATIVE.
     */
    @Column(name = "sentiment_label", length = 20)
    private String sentimentLabel;

    /**
     * Sentiment score in the range [-1.0, +1.0].
     * <ul>
     *   <li>-1.0 = strongly negative</li>
     *   <li> 0.0 = neutral</li>
     *   <li>+1.0 = strongly positive</li>
     * </ul>
     * If the ML service returns [0, 1], the listener normalises to [-1, +1]
     * via: {@code normalisedScore = (rawScore * 2) - 1}.
     */
    @Column(name = "sentiment_score")
    private Float sentimentScore;

    /**
     * Granular emotion breakdown — e.g. {"joy": 0.7, "sadness": 0.1, "anger": 0.05}.
     * All values in [0.0, 1.0]; they need not sum to 1.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion_vector", columnDefinition = "jsonb")
    private Map<String, Double> emotionVector;

    @Column(name = "ai_model_version", length = 50)
    private String aiModelVersion;

    @Column(name = "analysed_by", length = 50)
    @Builder.Default
    private String analysedBy = "SYSTEM";
}
