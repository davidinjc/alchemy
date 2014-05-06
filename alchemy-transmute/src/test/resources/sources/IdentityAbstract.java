package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("abstract")
public abstract class IdentityAbstract extends Identity {
    private final String value;

    public IdentityAbstract(String value) {
        this.value = value;
    }

    public String getValue() {
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