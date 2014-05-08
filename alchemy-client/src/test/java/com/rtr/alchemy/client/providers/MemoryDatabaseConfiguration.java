package com.rtr.alchemy.client.providers;

import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.memory.MemoryExperimentsStore;
import com.rtr.alchemy.service.config.ExperimentsStoreConfiguration;

public class MemoryDatabaseConfiguration extends ExperimentsStoreConfiguration {
    @Override
    public ExperimentsStore createStore() {
        return new MemoryExperimentsStore();
    }
}
