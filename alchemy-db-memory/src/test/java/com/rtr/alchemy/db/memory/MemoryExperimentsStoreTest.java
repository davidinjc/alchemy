package com.rtr.alchemy.db.memory;


import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.testing.db.ExperimentsStoreTest;

public class MemoryExperimentsStoreTest extends ExperimentsStoreTest {
    @Override
    protected ExperimentsStore createStore() {
        return new MemoryExperimentsStore();
    }
}
