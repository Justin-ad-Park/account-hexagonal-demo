package com.example.account.domain.model;

import java.util.Objects;

public class Account {
    private final String accountNumber;
    private final String name;
    private long balance;

    public Account(String accountNumber,
                   String name,
                   long balance) {
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.name = Objects.requireNonNull(name);
        this.balance = balance;
    }

    // ★ MyBatis가 Long을 기대할 때 대응하는 보조 생성자
    public Account(String accountNumber, String name, Long balance) {
        this(accountNumber, name, balance != null ? balance.longValue() : 0L);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public long getBalance() { return balance; }

    public void deposit(Amount amount) {
        if (amount.getValue() <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount.getValue();
    }

    public void withdraw(Amount amount) {
        if (amount.getValue() <= 0) throw new IllegalArgumentException("Withdraw must be positive");
        if (balance < amount.getValue()) throw new IllegalStateException("Insufficient balance");
        balance -= amount.getValue();
    }
}
