package com.example.account.application.port.in;

import com.example.account.application.port.in.command.WithdrawCommand;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;

public interface WithdrawUseCase {
    Account withdraw(WithdrawCommand withdrawCommand);
}
