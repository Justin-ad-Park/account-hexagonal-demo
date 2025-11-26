package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import com.example.account.adapter.in.web.dto.ApiResponse; // ApiResponse import 필요
import com.example.account.adapter.in.web.dto.ApiError;      // ApiError import 필요
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerFileTest {

    private static final String ACC_NO = "it-001";

    // 제네릭 타입을 쉽게 참조하기 위한 타입 참조 객체 정의
    private static final ParameterizedTypeReference<ApiResponse<AccountResponse>> ACCOUNT_API_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};


    @LocalServerPort int port;

    @Autowired TestRestTemplate restTemplate;

    private String url(String path) { return "http://localhost:" + port + path; }

    /**
     * TestConfiguration: 테스트 전용 빈 설정
     */
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
        assertThat(Files.exists(TestBeans.BASE_DIR.resolve(ACC_NO + ".txt"))).isTrue();
    }

    // 2) 입금
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

    // 3) 출금
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

    // 4) 계좌조회
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
        var response = restTemplate.exchange(
                url("/accounts/noAccount"),
                HttpMethod.GET,
                null,
                ACCOUNT_API_RESPONSE_TYPE
        );

        // 1. 상태 코드는 여전히 200 OK여야 함
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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