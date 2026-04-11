package com.syedsadiquh.coreservice.journal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalPageRequest {

    @NotNull
    private UUID tenantId;

    @NotEmpty
    private String title;

    private String description;

    private String coverImageUrl;

    @NotNull
    private LocalDate entryDate;

    private Boolean isPrivate = true;

    @Valid
    private List<CreateBlockRequest> blocks;

    private List<UUID> tagIds;
}
