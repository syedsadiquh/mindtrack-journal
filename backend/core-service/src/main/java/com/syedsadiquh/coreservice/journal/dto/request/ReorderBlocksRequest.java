package com.syedsadiquh.coreservice.journal.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderBlocksRequest {

    @NotEmpty
    private List<UUID> blockIds;
}
