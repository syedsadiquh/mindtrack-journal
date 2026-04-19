package com.syedsadiquh.coreservice.journal.util;

import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import com.syedsadiquh.coreservice.journal.entity.JournalPage;
import com.syedsadiquh.coreservice.journal.enums.BlockType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
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
     * Aggregates non-deleted narrative blocks (TEXT, HEADING, QUOTE) from a page
     * into a single string, ordered by block index, separated by newlines.
     * Media/structural blocks are skipped. Returns empty string if no text.
     */
    public static String aggregateText(JournalPage page) {
        if (page.getBlocks() == null || page.getBlocks().isEmpty()) {
            return "";
        }
        return page.getBlocks().stream()
                .filter(block -> !block.getDeleted())
                .filter(block -> ANALYSABLE_TYPES.contains(block.getType()))
                .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                .filter(block -> hasText(block.getContent()))
                .map(block -> block.getContent().get("text").toString())
                .collect(Collectors.joining("\n"));
    }
}
