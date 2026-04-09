package com.syedsadiquh.coreservice.journal.dto.request;

import com.syedsadiquh.coreservice.journal.enums.BlockType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlockRequest {

    @NotNull
    private BlockType type;

    private UUID parentBlockId;

    private Integer orderIndex = 0;

    private Map<String, Object> content;

    private Map<String, Object> metadata;
}
