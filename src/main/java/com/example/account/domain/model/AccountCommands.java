package com.example.account.domain.model;

/**
 * 도메인 변경을 한 곳으로 모으는 얇은 퍼사드.
 * - 같은 패키지에 있기 때문에 Account의 package-private 메서드 호출 가능
 * - 응용 계층은 이 정적 메서드만 사용하여 도메인 변경을 트리거
 */
public final class AccountCommands {
    private AccountCommands() {}

    public static Account create(String accountNumber, String name, long initialBalance) {
        // 생성 규칙을 한 곳에서 관리할 수 있음
        return Account.of(accountNumber, name, initialBalance);
    }

    public static Account deposit(Account account, Amount amount) {
        account.deposit(amount);
        return account;
    }

    public static Account withdraw(Account account, Amount amount) {
        account.withdraw(amount);
        return account;
    }
}
