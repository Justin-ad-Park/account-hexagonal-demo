package com.example.account.adapter.in.web;

import com.example.account.application.port.in.CreateAccountUseCase;
import com.example.account.application.port.in.DepositUseCase;
import com.example.account.application.port.in.GetAccountQuery;
import com.example.account.application.port.in.WithdrawUseCase;
import com.example.account.application.port.in.command.DepositCommand;
import com.example.account.application.port.in.command.WithdrawCommand;
import com.example.account.domain.model.Account;
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

    @MockBean
    private GetAccountQuery getAccountQuery;

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        // given
        Account account = Account.of("123", "Alice", 1000L);
        given(createAccountUseCase.createAccount("123", "Alice", 1000L))
                .willReturn(account);

        // when & then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "accountNumber": "123",
                              "name": "Alice",
                              "balance": 1000
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("123"))
                .andExpect(jsonPath("$.data.name").value("Alice"))
                .andExpect(jsonPath("$.data.balance").value(1000));
    }

    @Test
    void deposit_shouldReturnUpdatedAccount() throws Exception {
        // given
        Account account = Account.of("123", "Alice", 1500L);
        given(depositUseCase.deposit(ArgumentMatchers.any(DepositCommand.class)))
                .willReturn(account);

        // when & then
        mockMvc.perform(post("/accounts/123/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 500
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(1500));
    }

    @Test
    void withdraw_shouldReturnUpdatedAccount() throws Exception {
        // given
        Account account = Account.of("123", "Alice", 1200L);
        given(withdrawUseCase.withdraw(ArgumentMatchers.any(WithdrawCommand.class)))
                .willReturn(account);

        // when & then
        mockMvc.perform(post("/accounts/123/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 300
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(1200));
    }

    @Test
    void getAccount_shouldReturnAccount() throws Exception {
        // given
        Account account = Account.of("123", "Alice", 800L);
        given(getAccountQuery.getAccount(ArgumentMatchers.eq("123")))
                .willReturn(account);

        // when & then
        mockMvc.perform(get("/accounts/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(800));
    }
}