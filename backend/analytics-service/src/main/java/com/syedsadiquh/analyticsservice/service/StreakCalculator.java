package com.syedsadiquh.analyticsservice.service;

import java.time.LocalDate;
import java.util.List;

/**
 * Pure-function streak helpers. {@code dates} must be sorted DESC (most recent first).
 */
public final class StreakCalculator {

    private StreakCalculator() {}

    /**
     * Counts consecutive days from the most recent entry.
     * Accepts streaks starting today OR yesterday so users who haven't written
     * yet today don't lose their streak.
     */
    public static int currentStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate mostRecent = dates.getFirst();
        // Accept mostRecent ahead of server "today" (client TZ > server UTC) as current.
        if (mostRecent.isBefore(today.minusDays(1))) return 0;

        int streak = 0;
        LocalDate expected = mostRecent;
        for (LocalDate date : dates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     * Finds the longest consecutive run across all history.
     */
    public static int longestStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) return 0;
        int longest = 1;
        int current = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).minusDays(1))) {
                if (++current > longest) longest = current;
            } else {
                current = 1;
            }
        }
        return longest;
    }
}
