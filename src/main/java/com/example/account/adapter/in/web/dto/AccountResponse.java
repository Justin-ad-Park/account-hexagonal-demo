package com.example.account.adapter.in.web.dto;

import com.example.account.domain.model.Account;

// adapter/in/web/dto/AccountResponse.java
public class AccountResponse{
    private final String accountNumber;
    private final String name;
    private final long balance;

    private AccountResponse(String accountNumber, String name, long balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public long getBalance() { return balance; }

    public static AccountResponse byAccount(Account account) {
        return new AccountResponse(account.getAccountNumber(), account.getName(), account.getBalance());
    }

}
