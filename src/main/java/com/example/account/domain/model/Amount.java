package com.example.account.domain.model;

public class Amount {
    private final long value;

    public Amount(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
