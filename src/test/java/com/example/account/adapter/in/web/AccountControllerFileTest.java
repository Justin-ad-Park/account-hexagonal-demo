package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.*;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerFileTest  extends AccountControllerTest {
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

    @Test @Order(6)
    void 데이터파일_존재확인() {
        assertThat(Files.exists(TestBeans.BASE_DIR.resolve(ACC_NO + ".txt"))).isTrue();
    }
}