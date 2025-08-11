package com.example.account.adapter.in.web;

import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.domain.model.Account;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerRealTest {

    private static final String ACC_NO = "it-001";

    @LocalServerPort int port;

    @Autowired TestRestTemplate restTemplate;

    private String url(String path) { return "http://localhost:" + port + path; }

    /* Before : Ver01 */
//    @TestConfiguration
//    static class TestBeans {
//        // ~/test/accounts
//        static final Path BASE_DIR = Path.of(System.getProperty("user.home"), "test", "accounts");
//        static {
//            try { Files.createDirectories(BASE_DIR); }
//            catch (Exception e) { throw new RuntimeException("Failed to create test base dir", e); }
//        }
//
//        // 테스트에서는 Out Port만 테스트 경로로 교체
//        @Bean @Primary
//        LoadAccountPort testLoadPort() { return new FileAccountPersistenceAdapter(BASE_DIR); }
//
//        @Bean @Primary
//        SaveAccountPort testSavePort() { return new FileAccountPersistenceAdapter(BASE_DIR); }
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

    @Test @Order(0)
    void cleanup_existing_account_file_if_any() throws Exception {
        Path file = TestBeans.BASE_DIR.resolve(ACC_NO + ".txt");
        if (Files.exists(file)) {
            Path tmp = TestBeans.BASE_DIR.resolve(ACC_NO + ".txt.del");
            Files.move(file, tmp, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(tmp);
        }
        assertThat(Files.exists(file)).isFalse();
    }

    @Test @Order(1)
    void createAccount_shouldReturnCreatedAccount() {
        var created = restTemplate.postForEntity(
                url("/accounts?accountNumber=" + ACC_NO + "&name=Bob&balance=1000"),
                null,
                Account.class
        ).getBody();

        assertThat(created).isNotNull();
        assertThat(created.getAccountNumber()).isEqualTo(ACC_NO);
        assertThat(created.getName()).isEqualTo("Bob");
        assertThat(created.getBalance()).isEqualTo(1000L);
        assertThat(Files.exists(TestBeans.BASE_DIR.resolve(ACC_NO + ".txt"))).isTrue();
    }

    @Test @Order(2)
    void deposit_shouldIncreaseBalance() {
        var afterDeposit = restTemplate.postForEntity(
                url("/accounts/" + ACC_NO + "/deposit?amount=500"),
                null,
                Account.class
        ).getBody();
        assertThat(afterDeposit).isNotNull();
        assertThat(afterDeposit.getBalance()).isEqualTo(1500L);
    }

    @Test @Order(3)
    void withdraw_shouldDecreaseBalance() {
        var afterWithdraw = restTemplate.postForEntity(
                url("/accounts/" + ACC_NO + "/withdraw?amount=300"),
                null,
                Account.class
        ).getBody();
        assertThat(afterWithdraw).isNotNull();
        assertThat(afterWithdraw.getBalance()).isEqualTo(1200L);
    }
}
