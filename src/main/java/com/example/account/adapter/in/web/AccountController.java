package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                              DepositUseCase depositUseCase,
                              WithdrawUseCase withdrawUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
    }

    @PostMapping
    public AccountResponse create(@RequestParam String accountNumber,
                                  @RequestParam String name,
                                  @RequestParam long balance) {
        var acc = createAccountUseCase.createAccount(accountNumber, name, balance);
        return AccountResponse.byAccount(acc);
    }

    @PostMapping("/{accountNumber}/deposit")
    public AccountResponse deposit(@PathVariable String accountNumber, @RequestParam long amount) {
        var acc = depositUseCase.deposit(accountNumber, new Amount(amount));
        return AccountResponse.byAccount(acc);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public AccountResponse withdraw(@PathVariable String accountNumber, @RequestParam long amount) {
        var acc = withdrawUseCase.withdraw(accountNumber, new Amount(amount));
        return AccountResponse.byAccount(acc);
    }
}
