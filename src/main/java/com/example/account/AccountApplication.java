package com.example.account;

import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
import com.example.account.domain.service.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;

@SpringBootApplication
public class AccountApplication {

    @Bean
    public AccountService accountService() {
        FileAccountPersistenceAdapter adapter =
                new FileAccountPersistenceAdapter(Paths.get("data"));
        return new AccountService(adapter, adapter);
    }

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
