package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Page<JournalEntry> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Page<JournalEntry> findByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);

    Optional<JournalEntry> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
}

