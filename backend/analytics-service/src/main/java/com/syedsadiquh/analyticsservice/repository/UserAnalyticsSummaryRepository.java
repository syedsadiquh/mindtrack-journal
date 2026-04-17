package com.syedsadiquh.analyticsservice.repository;

import com.syedsadiquh.analyticsservice.entity.UserAnalyticsSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserAnalyticsSummaryRepository extends JpaRepository<UserAnalyticsSummary, UUID> {

    @Transactional
    @Modifying
    @Query("""
            UPDATE UserAnalyticsSummary s
            SET s.currentStreak = :currentStreak,
                s.longestStreak = :longestStreak,
                s.computedAt    = :now
            WHERE s.userId = :userId
            """)
    int updateStreaks(@Param("userId") UUID userId,
                     @Param("currentStreak") int currentStreak,
                     @Param("longestStreak") int longestStreak,
                     @Param("now") LocalDateTime now);
}
