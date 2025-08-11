package com.example.account.adapter.out.file;

import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import com.example.account.domain.model.Account;

import java.io.IOException;
import java.nio.file.*;

public class FileAccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final Path basePath;

    public FileAccountPersistenceAdapter(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public Account load(String accountNumber) {
        Path filePath = basePath.resolve(accountNumber + ".txt");
        try {
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("Account not found");
            }
            String[] lines = Files.readAllLines(filePath).toArray(new String[0]);
            String name = lines[0];
            long balance = Long.parseLong(lines[1]);
            return new Account(accountNumber, name, balance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load account", e);
        }
    }

    @Override
    public void save(Account account) {
        Path filePath = basePath.resolve(account.getAccountNumber() + ".txt");
        try {
            Files.createDirectories(basePath);
            Files.write(filePath, (account.getName() + "\n" + account.getBalance()).getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save account", e);
        }
    }
}
