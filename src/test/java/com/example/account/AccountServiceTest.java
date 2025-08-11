package com.example.account;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
import com.example.account.domain.model.Amount;
import com.example.account.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest {

    @Autowired CreateAccountUseCase createAccountUseCase;
    @Autowired DepositUseCase depositUseCase;
    @Autowired WithdrawUseCase withdrawUseCase;

    /* After ver01 */
//    @TestConfiguration
//    static class TestBeans {
//        static final Path TEMP_DIR;
//        static {
//            try { TEMP_DIR = Files.createTempDirectory("accounts-it"); }
//            catch (Exception e) { throw new RuntimeException(e); }
//        }
//
//        // 테스트 Out Port를 임시 디렉터리로 교체
//        @Bean @Primary
//        LoadAccountPort testLoadPort() { return new FileAccountPersistenceAdapter(TEMP_DIR); }
//
//        @Bean @Primary
//        SaveAccountPort testSavePort() { return new FileAccountPersistenceAdapter(TEMP_DIR); }
//    }

    /* After ver02 */
    @TestConfiguration
    static class TestBeans {
        static final Path BASE_DIR = Path.of(System.getProperty("user.home"), "test", "accounts");
        static {
            try { Files.createDirectories(BASE_DIR); }
            catch (Exception e) { throw new RuntimeException("Failed to create test base dir", e); }
        }

        // ✅ 동일하게 어댑터 하나만 우선순위로 등록
        @Bean @Primary
        FileAccountPersistenceAdapter testFileAdapter() {
            return new FileAccountPersistenceAdapter(BASE_DIR);
        }
    }

    @Test
    void createAndDepositAndWithdraw() {
        Account account = createAccountUseCase.createAccount("123", "Alice", 1000L);
        assertEquals(1000L, account.getBalance());

        account = depositUseCase.deposit("123", new Amount(500));
        assertEquals(1500L, account.getBalance());

        account = withdrawUseCase.withdraw("123", new Amount(300));
        assertEquals(1200L, account.getBalance());
    }
}
