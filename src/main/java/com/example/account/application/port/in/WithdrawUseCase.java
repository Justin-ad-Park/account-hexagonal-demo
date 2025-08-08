package com.example.account.application.port.in;

import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;

public interface WithdrawUseCase {
    Account withdraw(String accountNumber, Amount amount);
}
