package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.GetAccountQuery;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.AccountCommands;
import com.example.account.domain.model.Amount;
import com.example.account.internal.DomainCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/accounts")
@Validated
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetAccountQuery getAccountQuery;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                              DepositUseCase depositUseCase,
                              WithdrawUseCase withdrawUseCase,
                             GetAccountQuery getAccountQuery) {
        this.createAccountUseCase = createAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.getAccountQuery = getAccountQuery;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestParam @NotBlank String accountNumber,
                                                  @RequestParam @NotBlank String name,
                                                  @RequestParam @PositiveOrZero long balance) {
        var acc = createAccountUseCase.createAccount(accountNumber, name, balance);
        var body = AccountResponse.of(acc);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse>  deposit(@PathVariable @NotBlank String accountNumber, @RequestParam @PositiveOrZero long amount) {
        var acc = depositUseCase.deposit(accountNumber, new Amount(amount));
        var body = AccountResponse.of(acc);
        return ResponseEntity.ok(body); // 200 OK
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountResponse>  withdraw(@PathVariable @NotBlank String accountNumber, @RequestParam @PositiveOrZero  long amount) {
        var acc = withdrawUseCase.withdraw(accountNumber, new Amount(amount));
        var body = AccountResponse.of(acc);
        return ResponseEntity.ok(body); // 200 OK
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse>  getAccount(@PathVariable @NotBlank String accountNumber) {
        var acc = getAccountQuery.getAccount(accountNumber);
        var body = AccountResponse.of(acc);
        return ResponseEntity.ok(body); // 200 OK
    }

}
