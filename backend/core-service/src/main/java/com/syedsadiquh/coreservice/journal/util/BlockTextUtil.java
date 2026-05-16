package com.syedsadiquh.coreservice.journal.util;

import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import com.syedsadiquh.coreservice.journal.entity.JournalPage;
import com.syedsadiquh.coreservice.journal.enums.BlockType;
import com.syedsadiquh.coreservice.journal.service.JournalEncryptionService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockTextUtil {

    /** Block types that carry narrative text and should feed sentiment analysis. */
    public static final Set<BlockType> ANALYSABLE_TYPES =
            EnumSet.of(BlockType.TEXT, BlockType.HEADING, BlockType.QUOTE);

    /**
     * Returns true if the block content map contains a non-blank "text" entry.
     */
    public static boolean hasText(Map<String, Object> content) {
        if (content == null || !content.containsKey("text")) {
            return false;
        }
        Object text = content.get("text");
        return text != null && !text.toString().isBlank();
    }

    /**
     * Aggregates non-deleted narrative blocks from a page into a single string.
     * Block content is decrypted before extraction.
     */
    public static String aggregateText(JournalPage page, JournalEncryptionService encryptionService) {
        if (page.getBlocks() == null || page.getBlocks().isEmpty()) {
            return "";
        }
        return page.getBlocks().stream()
                .filter(block -> !block.getDeleted())
                .filter(block -> ANALYSABLE_TYPES.contains(block.getType()))
                .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                .map(block -> {
                    Map<String, Object> content = encryptionService.decryptMap(block.getContent());
                    return hasText(content) ? content.get("text").toString() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }
}
