package com.syedsadiquh.coreservice.journal.entity;

import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "journal_pages", indexes = {
        @Index(name = "idx_journal_page_user_id", columnList = "user_id"),
        @Index(name = "idx_journal_page_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_journal_page_entry_date", columnList = "entry_date"),
        @Index(name = "idx_journal_page_created_at", columnList = "created_at")
})
public class JournalPage extends BaseEntity {

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "is_private", nullable = false)
    @Builder.Default
    private Boolean isPrivate = true;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<JournalBlock> blocks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "page_tags",
            joinColumns = @JoinColumn(name = "page_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToOne(mappedBy = "page", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AiEnrichment aiEnrichment;
}
