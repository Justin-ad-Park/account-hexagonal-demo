package com.example.account.application.port.in;

import com.example.account.application.port.in.command.DepositCommand;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;

public interface DepositUseCase {
    Account deposit(DepositCommand depositCommand);
}
