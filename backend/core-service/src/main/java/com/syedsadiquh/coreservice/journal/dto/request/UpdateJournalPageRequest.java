package com.syedsadiquh.coreservice.journal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJournalPageRequest {
    private String title;
    private String description;
    private String coverImageUrl;
    private Boolean isPrivate;
}
