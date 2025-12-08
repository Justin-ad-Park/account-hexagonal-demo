package com.example.account.adapter.in.web.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

public record AmountRequest(
        @PositiveOrZero long amount
) {
}
