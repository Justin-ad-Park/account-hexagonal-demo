package com.example.account.adapter.in.web;

import com.example.account.adapter.out.file.FileAccountPersistenceAdapter;
import com.example.account.domain.model.Account;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerRealTestVer2 {

    private static final String ACC_NO = "it-001";

    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;

    // ✅ 실제 주입된 어댑터에서 현재 경로를 조회
    @Autowired FileAccountPersistenceAdapter fileAdapter;

    private String url(String path) { return "http://localhost:" + port + path; }
    private Path baseDir() { return fileAdapter.getBasePath(); } // 기존 테스트를 삭제할 수 있도록 경로(BasePath) 제공

    @Test @Order(0)
    void cleanup_existing_account_file_if_any() throws Exception {
        Files.createDirectories(baseDir());
        Path file = baseDir().resolve(ACC_NO + ".txt");
        if (Files.exists(file)) {
            Path tmp = baseDir().resolve(ACC_NO + ".txt.del");
            Files.move(file, tmp, StandardCopyOption.REPLACE_EXISTING); //기존 파일명 변경(백업)
            //Files.deleteIfExists(tmp);
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
        assertThat(Files.exists(baseDir().resolve(ACC_NO + ".txt"))).isTrue();
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
