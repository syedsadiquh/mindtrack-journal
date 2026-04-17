package com.syedsadiquh.analyticsservice.repository;

import com.syedsadiquh.analyticsservice.entity.DailySentimentCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailySentimentCacheRepository extends JpaRepository<DailySentimentCache, UUID> {

    @Query("""
            SELECT d FROM DailySentimentCache d
            WHERE d.userId = :userId
              AND d.entryDate BETWEEN :from AND :to
            ORDER BY d.entryDate ASC
            """)
    List<DailySentimentCache> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);

    @Query("""
            SELECT d.entryDate FROM DailySentimentCache d
            WHERE d.userId = :userId
            ORDER BY d.entryDate DESC
            """)
    List<LocalDate> findEntryDatesByUserIdDesc(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM DailySentimentCache d WHERE d.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
