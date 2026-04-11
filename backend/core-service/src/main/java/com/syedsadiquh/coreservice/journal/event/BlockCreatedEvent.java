package com.syedsadiquh.coreservice.journal.event;

import java.util.UUID;

public record BlockCreatedEvent(
        UUID blockId,
        String textContent
) {
}
