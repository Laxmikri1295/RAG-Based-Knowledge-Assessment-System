package com.bookreaderai.backend.repository;

import com.bookreaderai.backend.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QuizAttempt> findByUserIdAndBookNameOrderByCreatedAtDesc(Long userId, String bookName);
}
