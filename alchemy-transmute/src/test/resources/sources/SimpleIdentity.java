package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

@IdentityType("simple")
public class SimpleIdentity extends Identity {
    private final String name;
    private final String value;
    private final boolean active;

    public SimpleIdentity(String name, String value, boolean active) {
        this.name = name;
        this.value = value;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    // (is|has|get) is allowed, but '(is|has|get)Name' must match field 'name'
    public boolean isActive() {
        return active;
    }

    // Methods that don't start with (is|has|get) are ignored
    public boolean inactive() {
        return !active;
    }

    // Overriden methods are ignored
    @Override
    public boolean getActive() {
        return active;
    }

    @Override
    public long getHash(int seed) {
        return
            identity(seed)
                .putString(name)
                .putString(value)
                .putBoolean(active)
                .hash();
    }
}
