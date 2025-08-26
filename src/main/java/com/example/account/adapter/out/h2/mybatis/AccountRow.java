package com.example.account.adapter.out.h2.mybatis;

import com.example.account.domain.model.Account;

public class AccountRow {
    public AccountRow(String accountNumber, String name, Long balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }

    private String accountNumber;
    private String name;
    private Long balance; // DB에서 Long으로 매핑하면 null-safe

    public static AccountRow of(Account account) {
        return new AccountRow(account.getAccountNumber(), account.getName(), account.getBalance());
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getBalance() { return balance; }
    public void setBalance(Long balance) { this.balance = balance; }
}