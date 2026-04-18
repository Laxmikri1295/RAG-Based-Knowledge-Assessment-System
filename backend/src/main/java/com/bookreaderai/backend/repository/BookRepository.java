package com.bookreaderai.backend.repository;

import com.bookreaderai.backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Book}.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
