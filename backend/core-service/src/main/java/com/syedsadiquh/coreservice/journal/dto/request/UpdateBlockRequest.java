package com.syedsadiquh.coreservice.journal.dto.request;

import com.syedsadiquh.coreservice.journal.enums.BlockType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlockRequest {
    private BlockType type;
    private Integer orderIndex;
    private Map<String, Object> content;
    private Map<String, Object> metadata;
}
