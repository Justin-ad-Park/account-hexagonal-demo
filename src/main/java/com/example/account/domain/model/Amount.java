package com.example.account.domain.model;

public class Amount {
    private final long value;

    public Amount(long value) {
        this.value = value;
    }

    public Amount(Long value) {
        this.value = value != null ? value.longValue() : 0;
    }

    public long getValue() {
        return value;
    }
}
