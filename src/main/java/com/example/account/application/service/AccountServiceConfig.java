package com.example.account.application.service;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
    AccountService에 @Service 어노테이션을 붙이면 부모클래스에 @Config 어노테이션이 붙어 있어
    Spring이 구동될 때 Component Scan을 하기 때문에 아래와 같은 @Configuration이 필요없다.

    단, 동일 인터페이스를 반환하는 @Bean(Service)가 둘 이상 존재하면 오류가 나기 때문에
    @Service 어노테이션을 떼고, 아래와 같이 명시적으로 주입을 해야 한다.
    (AccountService2의 주석을 풀고 테스트하면 됨)
*/
/**
 * 서비스 구현(AccountService)은 패키지 내부에 숨기고,
 * 외부에는 Port 타입으로만 빈을 노출합니다.
 */
//@Configuration
//class AccountServiceConfig {
//
//    @Bean
//    AccountService2 accountService(LoadAccountPort load, SaveAccountPort save) {
//        return new AccountService2(load, save);
//    }
//
//    @Bean
//    CreateAccountUseCase createAccountUseCase(AccountService2 s) { return s; }
//
//    @Bean
//    DepositUseCase depositUseCase(AccountService2 s) { return s; }
//
//    @Bean
//    WithdrawUseCase withdrawUseCase(AccountService2 s) { return s; }
//}

