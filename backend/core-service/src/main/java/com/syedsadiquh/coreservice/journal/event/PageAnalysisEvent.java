package com.syedsadiquh.coreservice.journal.event;

import java.util.UUID;

/**
 * Published when a journal page's text content changes and needs re-analysis.
 * The listener aggregates all text blocks from the page before calling the ML service.
 */
public record PageAnalysisEvent(
        UUID pageId
) {
}
