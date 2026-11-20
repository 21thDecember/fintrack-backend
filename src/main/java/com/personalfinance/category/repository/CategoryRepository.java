package com.personalfinance.category.repository;

import com.personalfinance.category.entity.Category;
import com.personalfinance.category.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByIsActiveTrueOrderByNameAsc();

    List<Category> findAllByOrderByNameAsc();

    List<Category> findByTypeAndIsActiveTrueOrderByNameAsc(CategoryType type);

    List<Category> findByTypeOrderByNameAsc(CategoryType type);

    Optional<Category> findByIdAndIsActiveTrue(UUID id);

    Optional<Category> findFirstByNameIgnoreCaseAndTypeAndIsActiveTrue(String name, CategoryType type);

    List<Category> findByParentIdAndIsActiveTrue(UUID parentId);
}
