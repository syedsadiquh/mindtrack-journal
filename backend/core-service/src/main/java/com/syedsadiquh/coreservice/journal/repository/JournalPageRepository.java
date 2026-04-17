package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.dto.response.JournalPageAnalyticsDto;
import com.syedsadiquh.coreservice.journal.entity.JournalPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalPageRepository extends JpaRepository<JournalPage, UUID> {

    @EntityGraph(attributePaths = {"tags", "blocks"})
    Page<JournalPage> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"tags", "blocks"})
    Page<JournalPage> findByUserIdAndEntryDateBetweenAndDeletedFalse(
            UUID userId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = {"tags", "blocks"})
    Optional<JournalPage> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    @Query("SELECT p FROM JournalPage p JOIN p.tags t " +
           "WHERE p.userId = :userId AND t.id = :tagId AND p.deleted = false")
    Page<JournalPage> findByUserIdAndTagId(
            @Param("userId") UUID userId, @Param("tagId") UUID tagId, Pageable pageable);

    @Query("SELECT new com.syedsadiquh.coreservice.journal.dto.response.JournalPageAnalyticsDto(" +
           "p.entryDate, sa.sentimentLabel, sa.sentimentScore, sa.dominantEmotion) " +
           "FROM JournalPage p LEFT JOIN p.sentimentAnalysis sa " +
           "WHERE p.userId = :userId AND p.deleted = false " +
           "AND (CAST(:from AS date) IS NULL OR p.entryDate >= :from) " +
           "AND (CAST(:to AS date) IS NULL OR p.entryDate <= :to) " +
           "ORDER BY p.entryDate")
    List<JournalPageAnalyticsDto> findAnalyticsFeed(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
