package com.davivienda.app.controller;

import com.davivienda.app.security.UserPrincipal;
import com.davivienda.app.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;


@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {


    private final AnalyticsService analyticsService;


    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }


    @GetMapping("/category-summary")
    public ResponseEntity<?> categorySummary(@AuthenticationPrincipal UserPrincipal user,
                                             @RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to) {
        LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null ? LocalDate.parse(to) : null;

        return ResponseEntity.ok(analyticsService.categorySummary(user.getId(), fromDate, toDate));
    }



    @GetMapping("/monthly-balance")
    public ResponseEntity<?> monthlyBalance(@AuthenticationPrincipal UserPrincipal user,
                                            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(analyticsService.monthlyBalance(user.getId(), months));
    }


}