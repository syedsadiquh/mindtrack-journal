package com.syedsadiquh.coreservice.journal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.syedsadiquh.coreservice.journal.enums.BlockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalBlockResponse {
    private UUID id;
    private UUID parentBlockId;
    private BlockType type;
    private Integer orderIndex;
    private Map<String, Object> content;
    private Map<String, Object> metadata;
    private List<JournalBlockResponse> childBlocks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
