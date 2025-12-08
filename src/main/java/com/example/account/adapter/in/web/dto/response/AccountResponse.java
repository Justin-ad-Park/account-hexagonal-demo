package com.example.account.adapter.in.web.dto.response;

import com.example.account.domain.model.Account;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

// adapter/in/web/dto/AccountResponse.java
public class AccountResponse{
    @NotBlank
    private final String accountNumber;

    @NotBlank
    private final String name;

    @PositiveOrZero
    private final long balance;

    private AccountResponse(String accountNumber, String name, long balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public long getBalance() { return balance; }

    public static AccountResponse of(Account account) {
        return new AccountResponse(account.getAccountNumber(), account.getName(), account.getBalance());
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toString() {
        try {
            // AccountResponse 객체를 JSON 문자열로 변환
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // 문제가 생겨도 절대로 예외를 던지지 않고 fallback 문자열 출력
            return "AccountResponse{error converting to JSON}";
        }
    }
}
