package com.bookreaderai.backend.service;

import com.bookreaderai.backend.entity.QuizAttempt;
import com.bookreaderai.backend.entity.User;
import com.bookreaderai.backend.repository.QuizAttemptRepository;
import com.bookreaderai.backend.repository.UserRepository;
import com.bookreaderai.backend.dto.QuizSubmissionRequest;
import com.bookreaderai.backend.dto.DashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizEvaluationService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    private User getDefaultDemoUser() {
        List<User> allUsers = userRepository.findAll();
        if (!allUsers.isEmpty()) {
            return allUsers.get(0);
        }
        User newUser = new User();
        newUser.setEmailId("demo@example.com");
        newUser.setName("Demo User");
        newUser.setPassword("password");
        return userRepository.save(newUser);
    }

    public QuizAttempt evaluateAndSave(QuizSubmissionRequest request) {
        User user = getDefaultDemoUser();

        int total = request.answers().size();
        int correct = 0;
        for (var answer : request.answers()) {
            if (answer.isCorrect()) {
                correct++;
            }
        }

        double score = total == 0 ? 0 : ((double) correct / total) * 100;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setBookName(request.bookName());
        attempt.setTotalQuestions(total);
        attempt.setCorrectAnswers(correct);
        attempt.setScore(score);

        return quizAttemptRepository.save(attempt);
    }

    public DashboardStatsResponse getDashboardStats(Long userId) {
        // ALWAYS use the actual demo user id mapped in database, ignoring frontend hardcoded '1'
        Long realUserId = getDefaultDemoUser().getId();
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByCreatedAtDesc(realUserId);

        Map<String, List<QuizAttempt>> attemptsByBook = attempts.stream()
                .collect(Collectors.groupingBy(QuizAttempt::getBookName));

        List<DashboardStatsResponse.BookStat> bookStats = attemptsByBook.entrySet().stream()
                .map(entry -> {
                    String bookName = entry.getKey();
                    List<QuizAttempt> bookAttempts = entry.getValue();
                    double avg = bookAttempts.stream()
                            .mapToDouble(QuizAttempt::getScore)
                            .average()
                            .orElse(0.0);
                    return new DashboardStatsResponse.BookStat(bookName, bookAttempts.size(), avg);
                })
                .collect(Collectors.toList());

        List<DashboardStatsResponse.QuizHistoryRecord> history = attempts.stream()
                .limit(10) // last 10 attempts
                .map(a -> new DashboardStatsResponse.QuizHistoryRecord(
                        a.getBookName(),
                        a.getTotalQuestions(),
                        a.getCorrectAnswers(),
                        a.getScore(),
                        a.getCreatedAt().toString()))
                .collect(Collectors.toList());

        return new DashboardStatsResponse(bookStats, history);
    }
}
