package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.entity.SentimentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SentimentAnalysisRepository extends JpaRepository<SentimentAnalysis, UUID> {

    Optional<SentimentAnalysis> findByPageId(UUID pageId);
}
