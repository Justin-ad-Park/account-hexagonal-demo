package com.example.account.adapter.out.file;

import com.example.account.domain.model.Account;
import org.junit.jupiter.api.*;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileAccountPersistenceAdapterTest {

    static final Path BASE_DIR = Path.of(System.getProperty("user.home"), "test", "accounts");

    static FileAccountPersistenceAdapter fileAccountPersistenceAdapter;

    @BeforeAll
    static void setUp() {
        fileAccountPersistenceAdapter = new FileAccountPersistenceAdapter(BASE_DIR);
    }


    @Test @Order(1)
    void save() {
        Account account = Account.of("123", "Alice", 1000L);
        fileAccountPersistenceAdapter.save(account);
    }

    @Test @Order(2)
    void load() {
        String accountId = "123";
        Account account = fileAccountPersistenceAdapter.load(accountId);

        Assertions.assertEquals(1000L, account.getBalance());
    }
}