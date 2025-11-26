package com.davivienda.app.util;

import com.davivienda.app.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TransactionFilters {


    public static Specification<Transaction> filterBy(Long userId, Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
// mandatory: user
            predicates.add(cb.equal(root.get("user").get("id"), userId));


            if (filters == null) return cb.and(predicates.toArray(new Predicate[0]));


// date range
            if (filters.containsKey("from")) {
                LocalDate from = LocalDate.parse(filters.get("from"));
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (filters.containsKey("to")) {
                LocalDate to = LocalDate.parse(filters.get("to"));
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            }


// category - allow partial match (case-insensitive)
            if (filters.containsKey("category")) {
                String cat = filters.get("category").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("category")), "%" + cat + "%"));
            }


// type exact match
            if (filters.containsKey("type")) {
                String type = filters.get("type").toUpperCase();
                predicates.add(cb.equal(root.get("type"), type));
            }


// amount min / max
            if (filters.containsKey("minAmount")) {
                BigDecimal min = new BigDecimal(filters.get("minAmount"));
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), min));
            }
            if (filters.containsKey("maxAmount")) {
                BigDecimal max = new BigDecimal(filters.get("maxAmount"));
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), max));
            }


// full-text search in description
            if (filters.containsKey("q")) {
                String q = filters.get("q").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + q + "%"));
            }


// Order by date desc by default (handled in service pageable if needed)
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
