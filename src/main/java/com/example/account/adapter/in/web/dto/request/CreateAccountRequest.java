package com.example.account.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank String accountNumber,
        @NotBlank String name,
        @PositiveOrZero long balance
) {}