package com.rtr.alchemy.example.provider;

import com.rtr.alchemy.db.ExperimentsDatabaseProvider;
import com.rtr.alchemy.db.memory.MemoryDatabaseProvider;
import com.rtr.alchemy.service.config.DatabaseProviderConfiguration;

public class MemoryDatabaseConfiguration extends DatabaseProviderConfiguration {
    @Override
    public ExperimentsDatabaseProvider createProvider() {
        return new MemoryDatabaseProvider();
    }
}