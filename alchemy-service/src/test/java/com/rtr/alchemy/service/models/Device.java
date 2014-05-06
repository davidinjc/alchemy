package com.rtr.alchemy.service.models;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("device")
public class Device extends Identity {
    private final String id;

    public Device(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public long getHash(int seed) {
        return identity(seed).putString(id).hash();
    }
}