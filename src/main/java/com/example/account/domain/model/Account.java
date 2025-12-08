package com.example.account.domain.model;

import java.util.Objects;

public class Account {
    private final String accountNumber;
    private final String name;
    private long balance;

    // 생성자는 외부에서 직접 못 쓰게 private
    private Account(String accountNumber, String name, long balance) {
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.name = Objects.requireNonNull(name);
        this.balance = balance;
    }

    // 영속 어댑터/테스트가 사용할 수 있도록 공개 팩토리 제공
    public static Account of(String accountNumber, String name, long balance) {
        return new Account(accountNumber, name, balance);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public long getBalance() { return balance; }

    // 변경 메서드를 공개해서 Rich Aggregate로 사용
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