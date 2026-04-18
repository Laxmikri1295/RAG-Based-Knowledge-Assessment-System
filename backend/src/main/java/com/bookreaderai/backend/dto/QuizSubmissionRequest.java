package com.bookreaderai.backend.dto;

import java.util.List;

public record QuizSubmissionRequest(
        Long userId,
        String bookName,
        List<QuizAnswerRecord> answers) {
    public record QuizAnswerRecord(
            String question,
            String userAnswer,
            String correctAnswer,
            boolean isCorrect) {
    }
}
