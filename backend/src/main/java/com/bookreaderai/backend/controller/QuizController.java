package com.bookreaderai.backend.controller;

import com.bookreaderai.backend.dto.DashboardStatsResponse;
import com.bookreaderai.backend.dto.QuizSubmissionRequest;
import com.bookreaderai.backend.service.QuizEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // For Vite
public class QuizController {

    private final QuizEvaluationService quizEvaluationService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmissionRequest request) {
        var attempt = quizEvaluationService.evaluateAndSave(request);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardStatsResponse> getDashboard(@PathVariable Long userId) {
        var stats = quizEvaluationService.getDashboardStats(userId);
        return ResponseEntity.ok(stats);
    }
}
