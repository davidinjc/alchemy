package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("mismatched")
public class IdentityMismatchedNames extends Identity {
    private final String name;

    public IdentityMismatchedNames(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getHash(int seed) {
        return
            identity(seed)
                .putString(name)
                .hash();
    }
}
