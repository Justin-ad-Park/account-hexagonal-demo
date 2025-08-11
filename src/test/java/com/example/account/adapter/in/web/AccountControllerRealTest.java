package com.example.account.adapter.in.web;

import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
import com.example.account.domain.model.Account;
import com.example.account.domain.service.AccountService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true" // ★ 오버라이딩 허용
)
@TestMethodOrder(OrderAnnotation.class)
class AccountControllerRealTest {

    private static final String ACC_NO = "it-001";

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestBeans {
        // ~/test/accounts
        static final Path BASE_DIR = Path.of(System.getProperty("user.home"), "test", "accounts");
        static {
            try {
                Files.createDirectories(BASE_DIR);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Bean
        @Primary
        AccountService accountService() {
            var adapter = new FileAccountPersistenceAdapter(BASE_DIR);
            return new AccountService(adapter, adapter);
        }
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // 0) 기존 계좌 파일 정리 (최우선)
    @Test
    @Order(0)
    void cleanup_existing_account_file_if_any() throws Exception {
        Path file = TestBeans.BASE_DIR.resolve(ACC_NO + ".txt");
        if (Files.exists(file)) {
            Path tmp = TestBeans.BASE_DIR.resolve(ACC_NO + ".txt.del");
            Files.move(file, tmp, StandardCopyOption.REPLACE_EXISTING);
            //Files.deleteIfExists(tmp);
        }
        assertThat(Files.exists(file)).isFalse();
    }

    // 1) 생성
    @Test
    @Order(1)
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

    // 2) 입금
    @Test
    @Order(2)
    void deposit_shouldIncreaseBalance() {
        var afterDeposit = restTemplate.postForEntity(
                url("/accounts/" + ACC_NO + "/deposit?amount=500"),
                null,
                Account.class
        ).getBody();

        assertThat(afterDeposit).isNotNull();
        assertThat(afterDeposit.getBalance()).isEqualTo(1500L);
    }

    // 3) 출금
    @Test
    @Order(3)
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
