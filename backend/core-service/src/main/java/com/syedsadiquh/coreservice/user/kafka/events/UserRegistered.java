package com.syedsadiquh.coreservice.user.kafka.events;

import lombok.Builder;

@Builder
public record UserRegistered(String name, String email) {
}
