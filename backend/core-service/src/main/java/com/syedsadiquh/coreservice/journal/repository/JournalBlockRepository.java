package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.entity.JournalBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalBlockRepository extends JpaRepository<JournalBlock, UUID> {

    List<JournalBlock> findByPageIdAndParentBlockIsNullAndDeletedFalseOrderByOrderIndexAsc(UUID pageId);

    List<JournalBlock> findByPageIdAndDeletedFalseOrderByOrderIndexAsc(UUID pageId);

    Optional<JournalBlock> findByIdAndPageIdAndDeletedFalse(UUID id, UUID pageId);

    int countByPageIdAndDeletedFalse(UUID pageId);
}
