package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

public class IdentityNoAnnotation extends Identity {
    private final int value;

    public IdentityNoAnnotation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public long getHash(int seed) {
        return
            identity(seed)
                .putString(value)
                .hash();
    }
}