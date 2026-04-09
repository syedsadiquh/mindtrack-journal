package com.syedsadiquh.coreservice.journal.event;

import java.util.UUID;

public record BlockUpdatedEvent(
        UUID blockId,
        String textContent
) {
}
