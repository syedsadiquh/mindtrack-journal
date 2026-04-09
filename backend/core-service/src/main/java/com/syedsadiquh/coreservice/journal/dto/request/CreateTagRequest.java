package com.syedsadiquh.coreservice.journal.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {

    @NotNull
    private UUID tenantId;

    @NotEmpty
    private String name;

    private String color;
}
