package com.example.account.application.port.in.command;

public record WithdrawCommand(
        String accountNumber,
        long amount
) {
}
