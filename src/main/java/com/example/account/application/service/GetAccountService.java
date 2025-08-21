package com.example.account.application.service;

import com.example.account.application.port.in.GetAccountQuery;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.domain.model.Account;

public class GetAccountService implements GetAccountQuery {

    private final LoadAccountPort loadAccountPort;

    GetAccountService(LoadAccountPort loadAccountPort) {
        this.loadAccountPort = loadAccountPort;
    }


    @Override
    public Account getAccount(String accountNumber) {
        return loadAccountPort.load(accountNumber);
    }
}
