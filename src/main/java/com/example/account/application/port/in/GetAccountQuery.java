package com.example.account.application.port.in;

import com.example.account.domain.model.Account;

public interface GetAccountQuery {
    Account getAccount(String accountNumber);
}
