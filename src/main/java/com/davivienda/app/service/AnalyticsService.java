package com.davivienda.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface AnalyticsService {
    Map<String, Object> categorySummary(Long userId, LocalDate from, LocalDate to);
    List<Map<String, Object>> monthlyBalance(Long userId, int months);
}