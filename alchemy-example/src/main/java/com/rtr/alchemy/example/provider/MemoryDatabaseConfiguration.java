package com.rtr.alchemy.example.provider;

import com.rtr.alchemy.db.ExperimentsStoreProvider;
import com.rtr.alchemy.db.memory.MemoryStoreProvider;
import com.rtr.alchemy.service.config.DatabaseProviderConfiguration;

public class MemoryDatabaseConfiguration extends DatabaseProviderConfiguration {
    @Override
    public ExperimentsStoreProvider createProvider() {
        return new MemoryStoreProvider();
    }
}
