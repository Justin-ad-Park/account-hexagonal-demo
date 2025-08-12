package com.example.account.application.service;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서비스 구현(AccountService)은 패키지 내부에 숨기고,
 * 외부에는 Port 타입으로만 빈을 노출합니다.
 */
@Configuration
class ApplicationConfig {

    @Bean
    AccountService accountService(LoadAccountPort load, SaveAccountPort save) {
        return new AccountService(load, save);
    }

    @Bean
    CreateAccountUseCase createAccountUseCase(AccountService s) { return s; }

    @Bean
    DepositUseCase depositUseCase(AccountService s) { return s; }

    @Bean
    WithdrawUseCase withdrawUseCase(AccountService s) { return s; }
}
