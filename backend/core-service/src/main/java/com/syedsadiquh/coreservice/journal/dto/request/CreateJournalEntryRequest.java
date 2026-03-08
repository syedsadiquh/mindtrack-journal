package com.syedsadiquh.coreservice.journal.dto.request;

import com.syedsadiquh.coreservice.journal.enums.Mood;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalEntryRequest {

    @NotNull
    private UUID tenantId;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    private List<String> tags;

    private Mood mood;
}

