package com.example.account.application.port.out;

import com.example.account.domain.model.Account;

public interface SaveAccountPort {
    void save(Account account);
}
