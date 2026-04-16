package com.syedsadiquh.coreservice.journal.entity;

import com.syedsadiquh.coreservice.journal.enums.BlockType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "journal_blocks", indexes = {
        @Index(name = "idx_block_page_id", columnList = "page_id"),
        @Index(name = "idx_block_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_block_order", columnList = "page_id, order_index")
})
public class JournalBlock extends BaseEntity {

    @Column(nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private JournalPage page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    private JournalBlock parentBlock;

    @OneToMany(mappedBy = "parentBlock", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<JournalBlock> childBlocks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BlockType type;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL)
    @OrderBy("versionNumber DESC")
    @Builder.Default
    private List<BlockVersion> versions = new ArrayList<>();
}
