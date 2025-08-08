package com.example.account.domain.service;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;

public class AccountService implements CreateAccountUseCase, DepositUseCase, WithdrawUseCase {

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;

    public AccountService(LoadAccountPort loadAccountPort, SaveAccountPort saveAccountPort) {
        this.loadAccountPort = loadAccountPort;
        this.saveAccountPort = saveAccountPort;
    }

    @Override
    public Account createAccount(String accountNumber, String name, long initialBalance) {
        Account account = new Account(accountNumber, name, initialBalance);
        saveAccountPort.save(account);
        return account;
    }

    @Override
    public Account deposit(String accountNumber, Amount amount) {
        Account account = loadAccountPort.load(accountNumber);
        account.deposit(amount);
        saveAccountPort.save(account);
        return account;
    }

    @Override
    public Account withdraw(String accountNumber, Amount amount) {
        Account account = loadAccountPort.load(accountNumber);
        account.withdraw(amount);
        saveAccountPort.save(account);
        return account;
    }
}
