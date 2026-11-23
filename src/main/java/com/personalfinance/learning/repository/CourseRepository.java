package com.personalfinance.learning.repository;

import com.personalfinance.learning.entity.Course;
import com.personalfinance.learning.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    Optional<Course> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, CourseStatus status);

    List<Course> findByUserIdAndStatus(UUID userId, CourseStatus status);

    List<Course> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
