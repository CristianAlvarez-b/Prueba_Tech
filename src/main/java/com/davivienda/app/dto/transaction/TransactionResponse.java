package com.davivienda.app.dto.transaction;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private LocalDate date;
    private BigDecimal amount;
    private String category;
    private String type;
    private String description;
}