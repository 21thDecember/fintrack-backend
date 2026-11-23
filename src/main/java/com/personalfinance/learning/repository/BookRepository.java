package com.personalfinance.learning.repository;

import com.personalfinance.learning.entity.Book;
import com.personalfinance.learning.entity.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, BookStatus status);

    List<Book> findByUserIdAndStatus(UUID userId, BookStatus status);

    List<Book> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
