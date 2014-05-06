package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("multipleCtor")
public class IdentityMultipleConstructors extends Identity {
    private final String value;

    public IdentityMultipleConstructors(String value) {
        this.value = value;
    }

    public IdentityMultipleConstructors() {
        this.value = null;
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