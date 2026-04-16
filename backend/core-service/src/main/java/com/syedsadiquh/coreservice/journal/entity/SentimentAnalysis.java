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
        @Index(name = "idx_sentiment_page_id", columnList = "page_id")
})
public class SentimentAnalysis extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false, unique = true)
    private JournalPage page;

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
     * Derived from the dominant sentiment's confidence score.
     */
    @Column(name = "sentiment_score")
    private Float sentimentScore;

    /**
     * Full sentiment score breakdown — e.g. {"POSITIVE": 0.85, "NEGATIVE": 0.05, "NEUTRAL": 0.10}.
     * All values in [0.0, 1.0]; they sum to ~1.0.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sentiment_scores", columnDefinition = "jsonb")
    private Map<String, Double> sentimentScores;

    /**
     * The dominant emotion detected — e.g. "joy", "sadness", "anger".
     */
    @Column(name = "dominant_emotion", length = 30)
    private String dominantEmotion;

    /**
     * Granular emotion breakdown — e.g. {"joy": 0.7, "sadness": 0.1, "anger": 0.05}.
     * All values in [0.0, 1.0]; they sum to ~1.0.
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
