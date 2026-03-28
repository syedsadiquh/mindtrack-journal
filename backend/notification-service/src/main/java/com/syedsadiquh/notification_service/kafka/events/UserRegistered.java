package com.syedsadiquh.notification_service.kafka.events;

import lombok.Builder;

@Builder
public record UserRegistered(String name, String email) {
}
