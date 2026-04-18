package com.bookreaderai.backend.dto;

import java.util.List;

public record DashboardStatsResponse(
        List<BookStat> bookStats,
        List<QuizHistoryRecord> recentHistory) {
    public record BookStat(
            String bookName,
            int totalAttempts,
            double averageScore) {
    }

    public record QuizHistoryRecord(
            String bookName,
            int totalQuestions,
            int correctAnswers,
            double score,
            String takenAt) {
    }
}
