package com.example.account.application.port.out;

import com.example.account.domain.model.Account;

public interface LoadAccountPort {
    Account load(String accountNumber);
}
