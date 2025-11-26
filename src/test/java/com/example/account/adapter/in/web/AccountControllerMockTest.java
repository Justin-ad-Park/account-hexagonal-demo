package com.example.account.adapter.in.web;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.GetAccountQuery;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.domain.model.Account;
import com.example.account.domain.model.Amount;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class) // ← 컨트롤러만 로드
class AccountControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateAccountUseCase createAccountUseCase;

    @MockBean
    private DepositUseCase depositUseCase;

    @MockBean
    private WithdrawUseCase withdrawUseCase;

    @MockBean
    private GetAccountQuery getAccountQuery; // getAccountQuery는 이미 MockBean으로 선언되어 있으므로 재선언 불필요

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        Account account = Account.of("123", "Alice", 1000L);
        given(createAccountUseCase.createAccount("123", "Alice", 1000L))
                .willReturn(account);

        mockMvc.perform(post("/accounts")
                        .param("accountNumber", "123")
                        .param("name", "Alice")
                        .param("balance", "1000")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // 1. 상태 코드는 200 OK로 변경
                .andExpect(status().isOk())
                // 2. 응답 본문의 success 필드가 true인지 확인
                .andExpect(jsonPath("$.success").value(true))
                // 3. 실제 데이터는 data 필드 아래에 위치
                .andExpect(jsonPath("$.data.accountNumber").value("123"))
                .andExpect(jsonPath("$.data.name").value("Alice"))
                .andExpect(jsonPath("$.data.balance").value(1000));
    }

    @Test
    void deposit_shouldReturnUpdatedAccount() throws Exception {
        Account account = Account.of("123", "Alice", 1500L);
        given(depositUseCase.deposit(ArgumentMatchers.eq("123"), ArgumentMatchers.any(Amount.class)))
                .willReturn(account);

        mockMvc.perform(post("/accounts/123/deposit")
                        .param("amount", "500"))
                // 1. 상태 코드는 200 OK
                .andExpect(status().isOk())
                // 2. success 필드가 true인지 확인
                .andExpect(jsonPath("$.success").value(true))
                // 3. data 필드 아래의 balance 확인
                .andExpect(jsonPath("$.data.balance").value(1500));
    }

    @Test
    void withdraw_shouldReturnUpdatedAccount() throws Exception {
        Account account = Account.of("123", "Alice", 1200L);
        given(withdrawUseCase.withdraw(ArgumentMatchers.eq("123"), ArgumentMatchers.any(Amount.class)))
                .willReturn(account);

        mockMvc.perform(post("/accounts/123/withdraw")
                        .param("amount", "300"))
                // 1. 상태 코드는 200 OK
                .andExpect(status().isOk())
                // 2. success 필드가 true인지 확인
                .andExpect(jsonPath("$.success").value(true))
                // 3. data 필드 아래의 balance 확인
                .andExpect(jsonPath("$.data.balance").value(1200));
    }

    @Test
    void getAccount_shouldReturnAccount() throws Exception {
        Account account = Account.of("123", "Alice", 800L);
        given(getAccountQuery.getAccount(ArgumentMatchers.eq("123")))
                .willReturn(account);

        mockMvc.perform(get("/accounts/123"))
                // 1. 상태 코드는 200 OK
                .andExpect(status().isOk())
                // 2. success 필드가 true인지 확인
                .andExpect(jsonPath("$.success").value(true))
                // 3. data 필드 아래의 balance 확인
                .andExpect(jsonPath("$.data.balance").value(800));
    }
}