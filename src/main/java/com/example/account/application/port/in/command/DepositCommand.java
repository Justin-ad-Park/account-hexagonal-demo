package com.example.account.application.port.in.command;

public record DepositCommand(
        String accountNumber,
        long amount
) {
}
