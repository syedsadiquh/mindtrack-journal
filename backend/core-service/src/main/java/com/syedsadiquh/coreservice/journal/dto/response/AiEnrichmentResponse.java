package com.syedsadiquh.coreservice.journal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiEnrichmentResponse {
    private String summary;
    private List<String> keywords;
    private String dominantEmotion;
    private Map<String, Object> recommendation;
    private LocalDateTime generatedAt;
}
