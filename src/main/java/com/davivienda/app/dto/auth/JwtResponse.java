package com.davivienda.app.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
}