package com.syedsadiquh.coreservice.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {
    @JsonProperty("page_id")
    private String pageId;

    private String text;
}
