package com.example.account.application.port.in;

import com.example.account.domain.model.Account;

public interface CreateAccountUseCase {
    Account createAccount(String accountNumber, String name, long initialBalance);
}
