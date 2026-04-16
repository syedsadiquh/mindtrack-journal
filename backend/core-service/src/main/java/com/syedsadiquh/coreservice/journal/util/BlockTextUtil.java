package com.syedsadiquh.coreservice.journal.util;

import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import com.syedsadiquh.coreservice.journal.entity.JournalPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockTextUtil {

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
     * Aggregates all non-deleted text blocks from a page into a single string,
     * ordered by block index, separated by newlines. Returns empty string if no text.
     */
    public static String aggregateText(JournalPage page) {
        if (page.getBlocks() == null || page.getBlocks().isEmpty()) {
            return "";
        }
        return page.getBlocks().stream()
                .filter(block -> !block.getDeleted())
                .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                .filter(block -> hasText(block.getContent()))
                .map(block -> block.getContent().get("text").toString())
                .collect(Collectors.joining("\n"));
    }
}
