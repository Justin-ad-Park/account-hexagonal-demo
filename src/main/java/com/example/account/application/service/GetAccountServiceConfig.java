package com.example.account.application.service;

import com.example.account.application.port.in.GetAccountQuery;
import com.example.account.application.port.out.LoadAccountPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAccountServiceConfig {
    @Bean
    GetAccountService GetAccountService(LoadAccountPort load) {
        return new GetAccountService(load);
    }

    @Bean
    GetAccountQuery getAccountQuery(GetAccountService s) { return s; }
}
