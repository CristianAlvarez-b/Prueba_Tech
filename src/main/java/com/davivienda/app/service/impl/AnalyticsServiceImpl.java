package com.davivienda.app.service.impl;

import com.davivienda.app.service.AnalyticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @PersistenceContext
    private EntityManager em;

    // ------------------------------------------------------------
    // 1) RESUMEN POR CATEGORÍA
    // ------------------------------------------------------------
    @Override
    @Transactional
    public Map<String, Object> categorySummary(Long userId, LocalDate from, LocalDate to) {
        if (to == null) to = LocalDate.now();
        if (from == null) from = to.minusMonths(11).withDayOfMonth(1);

        String jpql = "SELECT t.category as category, SUM(t.amount) as total FROM Transaction t " +
                "WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to " +
                "GROUP BY t.category ORDER BY total DESC";

        List<Tuple> res = em.createQuery(jpql, Tuple.class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<Map<String, Object>> categories = res.stream().map(tuple -> {
            Map<String, Object> item = new HashMap<>();
            item.put("category", tuple.get("category", String.class));
            item.put("total", Optional.ofNullable(tuple.get("total", BigDecimal.class)).orElse(BigDecimal.ZERO));
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("from", from);
        result.put("to", to);
        result.put("categories", categories);
        return result;
    }

    // ------------------------------------------------------------
    // 2) BALANCE MENSUAL (últimos N meses)
    // ------------------------------------------------------------
    @Override
    @Transactional
    public List<Map<String, Object>> monthlyBalance(Long userId, int months) {
        LocalDate now = LocalDate.now();
        YearMonth start = YearMonth.from(now).minusMonths(months - 1);

        String jpql = "SELECT FUNCTION('YEAR', t.date) AS yr, FUNCTION('MONTH', t.date) AS mon, " +
                "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS income, " +
                "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense " +
                "FROM Transaction t WHERE t.user.id = :userId AND t.date >= :startDate " +
                "GROUP BY yr, mon ORDER BY yr, mon";

        List<Tuple> raw = em.createQuery(jpql, Tuple.class)
                .setParameter("userId", userId)
                .setParameter("startDate", start.atDay(1))
                .getResultList();

        Map<YearMonth, Map<String, Object>> mapped = new HashMap<>();

        for (Tuple t : raw) {
            YearMonth ym = YearMonth.of(
                    ((Number) t.get("yr")).intValue(),
                    ((Number) t.get("mon")).intValue()
            );

            BigDecimal income = Optional.ofNullable(t.get("income", BigDecimal.class)).orElse(BigDecimal.ZERO);
            BigDecimal expense = Optional.ofNullable(t.get("expense", BigDecimal.class)).orElse(BigDecimal.ZERO);

            Map<String, Object> map = new HashMap<>();
            map.put("yearMonth", ym.toString());
            map.put("income", income);
            map.put("expense", expense);
            map.put("balance", income.subtract(expense));

            mapped.put(ym, map);
        }

        // Completa meses sin transacciones
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            YearMonth ym = start.plusMonths(i);
            result.add(mapped.getOrDefault(ym, Map.of(
                    "yearMonth", ym.toString(),
                    "income", BigDecimal.ZERO,
                    "expense", BigDecimal.ZERO,
                    "balance", BigDecimal.ZERO
            )));
        }

        return result;
    }


}
