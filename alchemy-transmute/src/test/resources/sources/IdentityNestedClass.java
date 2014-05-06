package com.example;

import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;

public class IdentityNestedClass {
    @IdentityType("nested")
    public static class NestedIdentity extends Identity {
        private final String value;

        public NestedIdentity(String value) {
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
}