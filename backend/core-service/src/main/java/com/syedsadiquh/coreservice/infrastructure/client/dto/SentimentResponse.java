package com.syedsadiquh.coreservice.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResponse {
    @JsonProperty("entry_id")
    private String entryId;

    private String sentiment;

    private double score;
}

