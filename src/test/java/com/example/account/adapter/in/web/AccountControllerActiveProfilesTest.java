package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import com.example.account.adapter.in.web.dto.ApiResponse; // ApiResponse 클래스 import 필요
import com.example.account.adapter.in.web.dto.ApiError;      // ApiError 클래스 import 필요
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerActiveProfilesTest {

    private static final String ACC_NO = "it-001";
    // 제네릭 타입을 쉽게 참조하기 위한 타입 참조 객체
    private static final ParameterizedTypeReference<ApiResponse<AccountResponse>> ACCOUNT_API_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;

    private String url(String path) { return "http://localhost:" + port + path; }

    @Test @Order(1)
    void createAccount_shouldReturnCreatedAccount() {
        // exchange를 사용하여 ApiResponse<AccountResponse> 타입으로 응답을 받음
        var response = restTemplate.exchange(
                url("/accounts?accountNumber=" + ACC_NO + "&name=Bob&balance=1000"),
                HttpMethod.POST,
                null,
                ACCOUNT_API_RESPONSE_TYPE
        );

        var created = AccountTestHelper.extractData(response);

        assertThat(created.getAccountNumber()).isEqualTo(ACC_NO);
        assertThat(created.getName()).isEqualTo("Bob");
        assertThat(created.getBalance()).isEqualTo(1000L);
    }

    @Test @Order(2)
    void deposit_shouldIncreaseBalance() {
        var response = restTemplate.exchange(
                url("/accounts/" + ACC_NO + "/deposit?amount=500"),
                HttpMethod.POST,
                null,
                ACCOUNT_API_RESPONSE_TYPE
        );

        var afterDeposit = AccountTestHelper.extractData(response);
        assertThat(afterDeposit.getBalance()).isEqualTo(1500L);
    }

    @Test @Order(3)
    void withdraw_shouldDecreaseBalance() {
        var response = restTemplate.exchange(
                url("/accounts/" + ACC_NO + "/withdraw?amount=300"),
                HttpMethod.POST,
                null,
                ACCOUNT_API_RESPONSE_TYPE
        );

        var afterWithdraw = AccountTestHelper.extractData(response);
        assertThat(afterWithdraw.getBalance()).isEqualTo(1200L);
    }

    @Test @Order(4)
    void 계좌조회() {
        var response = restTemplate.exchange(
                url("/accounts/" + ACC_NO),
                HttpMethod.GET,
                null,
                ACCOUNT_API_RESPONSE_TYPE
        );

        var account = AccountTestHelper.extractData(response);

        assertThat(account.getBalance()).isEqualTo(1200L);
    }


    @Test @Order(5)
    void 없는계좌조회_shouldHandleNotFound() {
        // 실패 응답은 데이터 타입이 null이므로 <Object>로 받거나,
        // 테스트의 일관성을 위해 <AccountResponse>를 유지하되 데이터 필드는 무시합니다.
        var response = restTemplate.exchange(
                url("/accounts/noAccount"),
                HttpMethod.GET,
                null,
                ACCOUNT_API_RESPONSE_TYPE
        );

        // 1. 상태 코드는 NOT_FOUND
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        var apiResponse = response.getBody();
        assertThat(apiResponse).isNotNull();

        // 2. success 필드가 false인지 확인 (비정상 로직 처리)
        assertThat(apiResponse.isSuccess()).isFalse();

        // 3. 에러 정보 확인
        ApiError error = apiResponse.getError();
        assertThat(error).isNotNull();
        // AccountNotFoundException을 GlobalExceptionHandler에서 "NOT_FOUND"로 처리했는지 확인
        assertThat(error.getCode()).isEqualTo("NOT_FOUND");
    }
}