// src/main/java/com/example/account/adapter/out/h2/H2AccountPersistenceAdapter.java
package com.example.account.adapter.out.h2;

import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.domain.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.NoSuchElementException;

public class H2AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final JdbcTemplate jdbcTemplate;

    public H2AccountPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Account> ROW_MAPPER = (rs, i) ->
            new Account(
                    rs.getString("account_number"),
                    rs.getString("name"),
                    rs.getLong("balance")
            );

    @Override
    public Account load(String accountNumber) {
        var list = jdbcTemplate.query(
                "SELECT account_number, name, balance FROM account WHERE account_number = ?",
                ROW_MAPPER, accountNumber
        );
        if (list.isEmpty()) {
            throw new NoSuchElementException("Account not found: " + accountNumber);
        }
        return list.get(0);
    }

    @Override
    public void save(Account account) {
        // upsert (MERGE) — H2 지원
        jdbcTemplate.update(
                "MERGE INTO account KEY(account_number) VALUES(?, ?, ?)",
                account.getAccountNumber(), account.getName(), account.getBalance()
        );
    }
}
