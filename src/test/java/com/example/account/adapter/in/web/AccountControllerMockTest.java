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

@WebMvcTest(AccountController.class)
class AccountControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateAccountUseCase createAccountUseCase;

    @MockBean
    private DepositUseCase depositUseCase;

    @MockBean
    private WithdrawUseCase withdrawUseCase;

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        Account account = new Account("123", "Alice", 1000L);
        given(createAccountUseCase.createAccount("123", "Alice", 1000L))
                .willReturn(account);

        mockMvc.perform(post("/accounts")
                        .param("accountNumber", "123")
                        .param("name", "Alice")
                        .param("balance", "1000")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void deposit_shouldReturnUpdatedAccount() throws Exception {
        Account account = new Account("123", "Alice", 1500L);
        given(depositUseCase.deposit(ArgumentMatchers.eq("123"), ArgumentMatchers.any(Amount.class)))
                .willReturn(account);

        mockMvc.perform(post("/accounts/123/deposit")
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void withdraw_shouldReturnUpdatedAccount() throws Exception {
        Account account = new Account("123", "Alice", 1200L);
        given(withdrawUseCase.withdraw(ArgumentMatchers.eq("123"), ArgumentMatchers.any(Amount.class)))
                .willReturn(account);

        mockMvc.perform(post("/accounts/123/withdraw")
                        .param("amount", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1200));
    }

    @MockBean
    private GetAccountQuery getAccountQuery;

    @Test
    void getAccount_shouldReturnAccount() throws Exception {
        Account account = new Account("123", "Alice", 800L);
        given(getAccountQuery.getAccount(ArgumentMatchers.eq("123")))
                .willReturn(account);

        mockMvc.perform(get("/accounts/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800));
    }


}
