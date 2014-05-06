package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("notPublic")
class IdentityNotPublic extends Identity {
    private final String value;

    public IdentityNotPublic(String value) {
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