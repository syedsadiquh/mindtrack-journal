package com.syedsadiquh.coreservice.journal.entity;

import com.syedsadiquh.coreservice.journal.enums.Mood;
import com.syedsadiquh.coreservice.journal.enums.SentimentLabel;
import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_journal_user_id", columnList = "userId"),
        @Index(name = "idx_journal_tenant_id", columnList = "tenantId"),
        @Index(name = "idx_journal_created_at", columnList = "createdAt")
})
public class JournalEntry extends BaseEntity {

    /**
     * Reference to the user who owns this entry.
     * Deliberately NOT a JPA @ManyToOne — no cross-schema FK.
     * The userId is extracted from the JWT security context.
     */
    @Column(nullable = false)
    private UUID userId;

    /**
     * Reference to the tenant context (for multi-tenant isolation).
     * No cross-schema FK — just a UUID reference.
     */
    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String title;

    /**
     * Plaintext content (may be null if only encrypted content is stored).
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Application-level encrypted content. Populated before persistence.
     * Scaffold column — encryption implementation deferred.
     */
    @Column(name = "encrypted_content", columnDefinition = "TEXT")
    private String encryptedContent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "journal_entry_tags",
            joinColumns = @JoinColumn(name = "entry_id")
    )
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Mood mood;

    /**
     * Sentiment score returned by the ML service (0.0 to 1.0).
     * Populated asynchronously after entry creation.
     */
    @Column(name = "sentiment_score")
    private Double sentimentScore;

    /**
     * Sentiment label returned by the ML service.
     * Populated asynchronously after entry creation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label")
    private SentimentLabel sentimentLabel;
}

