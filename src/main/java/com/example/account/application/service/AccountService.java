package com.example.account.application.service;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [ver02 변경사항]
 * public class와 생성자를 package-private으로 변경
 *
 * - package-private: 외부 패키지에서 직접 접근 금지.
 * - 외부에서는 오직 Port 인터페이스(Create/Deposit/WithdrawUseCase)로만 접근합니다.
 */
@Service // 유스케이스 구현은 애플리케이션 계층의 빈
@Transactional
class AccountService implements CreateAccountUseCase, DepositUseCase, WithdrawUseCase {

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;

    AccountService(LoadAccountPort loadAccountPort, SaveAccountPort saveAccountPort) {
        this.loadAccountPort = loadAccountPort;
        this.saveAccountPort = saveAccountPort;
    }

    @Override
    public Account createAccount(String accountNumber, String name, long initialBalance) {
        Account account = Account.of(accountNumber, name, initialBalance);
        saveAccountPort.save(account);
        return account;
    }

    @Override
    public Account deposit(String accountNumber, long amount) {
        Account account = loadAccountPort.load(accountNumber);
        account.deposit(new Amount(amount));
        saveAccountPort.save(account);
        return account;
    }

    @Override
    public Account withdraw(String accountNumber, long amount) {
        Account account = loadAccountPort.load(accountNumber);
        account.withdraw(new Amount(amount));
        saveAccountPort.save(account);
        return account;
    }
}