# 5. domain commands 모델 적용
Date: 2025-08-27

# status
 Applied

# Context
도메인 Entity 객체를 OOP 설계에 맞춰 활용하려면 메서드를 가진 RichEntity로 만들어야 하는데, 
Entity 객체의 메서드는 Service에서만 사용해야 하는 접근 제어를 해야 한다.

Java에서 접근 제어(한정자)는 public, private, package-private만 제공이 되는데, 
domain과 service는 패키지가 달라서 위의 방식으로는 접근 제어를 할 수 없다.

이를 해결하기 위해 RichEntity의 정책을 처리하는 메서드를 커맨드 클래스로 위임하고, 
Entity의 메서드는 package-private으로 접근을 제한해서 
RichEntity 객체를 직접 참조하는 다른 패키지에서는 메소드를 사용하지 못하도록 제안했다. 


# Decision
- AccountCommands : RichEntity의 package-private 메서드를 위임
```java
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
```

- Account : Service에서만 사용해야 하는 메서드를 package-private 으로 변경 
```java
public final class Account {
  ... 생략 ...
  // ★ 변경 메서드는 package-private 로 축소(접근 제한자 없음)
  void deposit(Amount amount) {
    if (amount.getValue() <= 0) throw new IllegalArgumentException("Deposit must be positive");
    balance += amount.getValue();
  }

  void withdraw(Amount amount) {
    if (amount.getValue() <= 0) throw new IllegalArgumentException("Withdraw must be positive");
    if (balance < amount.getValue()) throw new IllegalStateException("Insufficient balance");
    balance -= amount.getValue();
  }

}
```

- AccountService : AccountCommands를 통해 RichEntity의 메서드 사용
```java

class AccountService implements CreateAccountUseCase, DepositUseCase, WithdrawUseCase {
  ... 생략 ...
  @Override
  public Account createAccount(String accountNumber, String name, long initialBalance) {
    Account account = AccountCommands.create(accountNumber, name, initialBalance);
    saveAccountPort.save(account);
    return account;
  }
  
  @Override
  public Account deposit(String accountNumber, Amount amount) {
    Account account = loadAccountPort.load(accountNumber);
    AccountCommands.deposit(account, amount);
    saveAccountPort.save(account);
    return account;
  }
  
  @Override
  public Account withdraw(String accountNumber, Amount amount) {
    Account account = loadAccountPort.load(accountNumber);
    AccountCommands.withdraw(account, amount);
    saveAccountPort.save(account);
    return account;
  }
}
```



# Consequences
- 이렇게 해서 Entity를 다른 패키지에서도 값 객체 수준에서 사용 가능하도록 개방
  - 단순한 도메인에서는 in/out 전용 DTO를 만들지 않고, Entity를 바로 사용하는 것이 유지보수에 유리함
  - 그렇지만 다른 패키지에서 Entity의 메서드를 사용하는 것은 금지해야 도메인 중심으로 비즈니스 로직을 관리할 수 있음
- Commands를 다른 패키지에서 사용하는 것은 정책적으로 금지하거나, archunit 제약 조건 검사

