package com.personalfinance.transaction.repository;

import com.personalfinance.transaction.dto.TransactionFilterRequest;
import com.personalfinance.transaction.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilter(TransactionFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getAccountId() != null) {
                predicates.add(cb.equal(root.get("accountId"), filter.getAccountId()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getDirection() != null) {
                predicates.add(cb.equal(root.get("direction"), filter.getDirection()));
            }

            if (filter.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), filter.getSource()));
            }

            if (filter.getClassificationStatus() != null) {
                predicates.add(cb.equal(root.get("classificationStatus"), filter.getClassificationStatus()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getEndDate()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), pattern);
                Predicate origDescPredicate = cb.like(cb.lower(root.get("originalDescription")), pattern);
                Predicate notePredicate = cb.like(cb.lower(root.get("note")), pattern);
                predicates.add(cb.or(descPredicate, origDescPredicate, notePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
