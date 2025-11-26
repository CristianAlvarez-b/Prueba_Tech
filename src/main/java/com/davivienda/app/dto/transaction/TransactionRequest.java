package com.davivienda.app.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    @NotNull
    private LocalDate date;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;
    @NotBlank
    private String category;
    @NotBlank
    private String type; // INCOME or EXPENSE
    private String description;
}
