package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.entity.AiEnrichment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiEnrichmentRepository extends JpaRepository<AiEnrichment, UUID> {

    Optional<AiEnrichment> findByPageId(UUID pageId);
}
