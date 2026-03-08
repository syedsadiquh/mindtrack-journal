package com.syedsadiquh.coreservice.journal.dto.request;

import com.syedsadiquh.coreservice.journal.enums.Mood;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJournalEntryRequest {
    private String title;
    private String content;
    private List<String> tags;
    private Mood mood;
}

