package com.example.account.application.service.doc;

/**
 * [ver02 변경사항]
 * public class와 생성자를 package-private으로 변경
 *
 * - package-private: 외부 패키지에서 직접 접근 금지.
 * - 외부에서는 오직 Port 인터페이스(Create/Deposit/WithdrawUseCase)로만 접근합니다.
 */
//@Service // 동일 인터페이스의 빈이 겹치는 경우 테스트
//@Transactional
//class AccountService2 implements CreateAccountUseCase, DepositUseCase, WithdrawUseCase {
//
//    private final LoadAccountPort loadAccountPort;
//    private final SaveAccountPort saveAccountPort;
//
//    AccountService2(LoadAccountPort loadAccountPort, SaveAccountPort saveAccountPort) {
//        this.loadAccountPort = loadAccountPort;
//        this.saveAccountPort = saveAccountPort;
//    }
//
//    @Override
//    public Account createAccount(String accountNumber, String name, long initialBalance) {
//        Account account = AccountCommands.create(accountNumber, name, initialBalance);
//        saveAccountPort.save(account);
//        return account;
//    }
//
//    @Override
//    public Account deposit(String accountNumber, Amount amount) {
//        Account account = loadAccountPort.load(accountNumber);
//        AccountCommands.deposit(account, amount);
//        saveAccountPort.save(account);
//        return account;
//    }
//
//    @Override
//    public Account withdraw(String accountNumber, Amount amount) {
//        Account account = loadAccountPort.load(accountNumber);
//        AccountCommands.withdraw(account, amount);
//        saveAccountPort.save(account);
//        return account;
//    }
//}