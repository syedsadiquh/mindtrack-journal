package com.syedsadiquh.coreservice.journal.util;

import com.syedsadiquh.coreservice.journal.dto.response.JournalBlockResponse;
import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import com.syedsadiquh.coreservice.journal.service.JournalEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JournalBlockMapper {

    private final JournalEncryptionService encryptionService;

    public JournalBlockResponse toResponse(JournalBlock block) {
        List<JournalBlockResponse> children = null;
        if (block.getChildBlocks() != null && !block.getChildBlocks().isEmpty()) {
            children = block.getChildBlocks().stream()
                    .filter(b -> !b.getDeleted())
                    .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                    .map(this::toResponse)
                    .toList();
        }

        return JournalBlockResponse.builder()
                .id(block.getId())
                .parentBlockId(block.getParentBlock() != null ? block.getParentBlock().getId() : null)
                .type(block.getType())
                .orderIndex(block.getOrderIndex())
                .content(encryptionService.decryptMap(block.getContent()))
                .metadata(encryptionService.decryptMap(block.getMetadata()))
                .childBlocks(children)
                .createdAt(block.getCreatedAt())
                .updatedAt(block.getUpdatedAt())
                .build();
    }
}
