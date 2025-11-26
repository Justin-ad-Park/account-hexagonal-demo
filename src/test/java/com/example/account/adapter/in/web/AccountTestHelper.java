package com.example.account.adapter.in.web;

import com.example.account.adapter.in.web.dto.AccountResponse;
import com.example.account.adapter.in.web.dto.ApiResponse;
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


}
