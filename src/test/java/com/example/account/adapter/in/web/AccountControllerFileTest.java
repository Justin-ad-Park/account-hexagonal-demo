package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
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
class AccountControllerFileTest {

    private static final String ACC_NO = "it-001";


    @LocalServerPort int port;

    @Autowired TestRestTemplate restTemplate;

    private String url(String path) { return "http://localhost:" + port + path; }

    /**
     *  */
    @TestConfiguration
    static class TestBeans {
        static final Path BASE_DIR = Path.of(System.getProperty("user.home"), "test", "accounts");
        static {
            try { Files.createDirectories(BASE_DIR); }
            catch (Exception e) { throw new RuntimeException("Failed to create test base dir", e); }
        }

        // ✅ 파일 어댑터를 우선순위로 등록
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
            //Files.deleteIfExists(tmp);
        }
        assertThat(Files.exists(file)).isFalse();
    }

    @Test @Order(1)
    void createAccount_shouldReturnCreatedAccount() {
        var created = restTemplate.postForEntity(
                url("/accounts?accountNumber=" + ACC_NO + "&name=Bob&balance=1000"),
                null,
                AccountResponse.class
        ).getBody();

        assertThat(created).isNotNull();
        assertThat(created.getAccountNumber()).isEqualTo(ACC_NO);
        assertThat(created.getName()).isEqualTo("Bob");
        assertThat(created.getBalance()).isEqualTo(1000L);
        assertThat(Files.exists(TestBeans.BASE_DIR.resolve(ACC_NO + ".txt"))).isTrue();
    }

    // 2) 입금
    @Test @Order(2)
    void deposit_shouldIncreaseBalance() {
        var afterDeposit = restTemplate.postForEntity(
                url("/accounts/" + ACC_NO + "/deposit?amount=500"),
                null,
                AccountResponse.class
        ).getBody();

        assertThat(afterDeposit).isNotNull();
        assertThat(afterDeposit.getBalance()).isEqualTo(1500L);
    }

    // 3) 출금
    @Test @Order(3)
    void withdraw_shouldDecreaseBalance() {
        var afterWithdraw = restTemplate.postForEntity(
                url("/accounts/" + ACC_NO + "/withdraw?amount=300"),
                null,
                AccountResponse.class
        ).getBody();

        assertThat(afterWithdraw).isNotNull();
        assertThat(afterWithdraw.getBalance()).isEqualTo(1200L);
    }

    // 4) 계좌조회
    @Test @Order(4)
    void 계좌조회() {
        var account = restTemplate.getForEntity(
                url("/accounts/" + ACC_NO),
                AccountResponse.class
        ).getBody();

        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(1200L);
    }
}
