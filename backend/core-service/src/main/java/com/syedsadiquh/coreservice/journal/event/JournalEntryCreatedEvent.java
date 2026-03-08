package com.syedsadiquh.coreservice.journal.event;

import java.util.UUID;

/**
 * Domain event published after a journal entry is successfully persisted.
 * Consumed asynchronously by the sentiment analysis listener to trigger
 * the ML Feign call without blocking the main HTTP thread.
 */
public record JournalEntryCreatedEvent(
        UUID entryId,
        String content
) {
}

