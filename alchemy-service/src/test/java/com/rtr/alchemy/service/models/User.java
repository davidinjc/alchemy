package com.rtr.alchemy.service.models;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("user")
public class User extends Identity {
    private final String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getHash(int seed) {
        return identity(seed).putString(name).hash();
    }
}