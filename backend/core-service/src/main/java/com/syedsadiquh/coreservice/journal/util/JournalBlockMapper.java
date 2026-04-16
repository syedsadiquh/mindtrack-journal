package com.syedsadiquh.coreservice.journal.util;

import com.syedsadiquh.coreservice.journal.dto.response.JournalBlockResponse;
import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JournalBlockMapper {

    public static JournalBlockResponse toResponse(JournalBlock block) {
        List<JournalBlockResponse> children = null;
        if (block.getChildBlocks() != null && !block.getChildBlocks().isEmpty()) {
            children = block.getChildBlocks().stream()
                    .filter(b -> !b.getDeleted())
                    .sorted(Comparator.comparingInt(JournalBlock::getOrderIndex))
                    .map(JournalBlockMapper::toResponse)
                    .toList();
        }

        return JournalBlockResponse.builder()
                .id(block.getId())
                .parentBlockId(block.getParentBlock() != null ? block.getParentBlock().getId() : null)
                .type(block.getType())
                .orderIndex(block.getOrderIndex())
                .content(block.getContent())
                .metadata(block.getMetadata())
                .childBlocks(children)
                .createdAt(block.getCreatedAt())
                .updatedAt(block.getUpdatedAt())
                .build();
    }
}
