package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.response.AccountResponse;
import com.example.account.adapter.in.web.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountTestHelper {

    // ApiResponse를 받아 응답 본문에서 AccountResponse 데이터를 추출하는 헬퍼 메서드
    public static AccountResponse extractData(ResponseEntity<ApiResponse<AccountResponse>> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); // 상태 코드는 항상 200 OK
        var apiResponse = response.getBody();
        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.isSuccess()).isTrue(); // 성공 여부 확인
        return apiResponse.getData();
    }


    public static void printData(Object caller, ResponseEntity<ApiResponse<AccountResponse>> response) {
        String className = caller.getClass().getSimpleName();

        // 호출자(Caller) 메서드명 추출
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String callerMethod = "unknown";

        for (StackTraceElement e : stack) {
            // Test 메서드는 junit 프레임워크가 호출하므로 여기서 필터링
            if (e.getClassName().startsWith("com.example")
                    && !e.getMethodName().equals("printData")) {
                callerMethod = e.getMethodName();
                break;
            }
        }

        System.out.println("""
            ###### TEST OUTPUT ######
            TestClass : %s
            TestMethod: %s
            Response  : %s
            ##########################
            """.formatted(
                className,
                callerMethod,
                response.getBody().getData()
        ));
    }
}
