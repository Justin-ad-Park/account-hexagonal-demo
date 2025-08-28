package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @SpringBootTest
 *
 * SpringBootTestContextBootstrapper → SpringBootContextLoader
 * 전체 애플리케이션 컨텍스트(내장 서버 여부, webEnvironment)까지 고려
 * SpringApplication 경로로 빈 등록/오토컨피그 전부 활성화
 */
@ActiveProfiles("h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerActiveProfilesTest {

    private static final String ACC_NO = "it-001";

    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;

    private String url(String path) { return "http://localhost:" + port + path; }

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
    }

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


    @Test @Order(5)
    void 없는계좌조회_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/accounts/noAccount"),
                String.class
        );

        // 없는 계좌일 경우 404 응답을 기대
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
