package com.example.account;

import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
import com.example.account.domain.model.Amount;
import com.example.account.domain.service.AccountService;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("accounts");
        FileAccountPersistenceAdapter adapter = new FileAccountPersistenceAdapter(tempDir);
        accountService = new AccountService(adapter, adapter);
    }

    @Test
    void createAndDepositAndWithdraw() {
        var account = accountService.createAccount("123", "Alice", 1000L);
        assertEquals(1000L, account.getBalance());

        account = accountService.deposit("123", new Amount(500));
        assertEquals(1500L, account.getBalance());

        account = accountService.withdraw("123", new Amount(300));
        assertEquals(1200L, account.getBalance());
    }
}
