package com.example.account.adapter.out.h2;

import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.domain.model.Account;
import com.example.account.adapter.out.h2.mapper.AccountMapper;

/**
 * 쿼리는 모두 MyBatis XML로 분리.
 * 어댑터는 Port 구현과 도메인 변환만 담당.
 */
class H2AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final AccountMapper mapper;

    H2AccountPersistenceAdapter(AccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Account load(String accountNumber) {
        Account account = mapper.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account;
    }

    @Override
    public void save(Account account) {
        mapper.upsert(account);
    }
}
