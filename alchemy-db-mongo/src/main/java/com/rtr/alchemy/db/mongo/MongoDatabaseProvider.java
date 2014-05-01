package com.rtr.alchemy.db.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.rtr.alchemy.db.ExperimentsDatabaseProvider;
import com.rtr.alchemy.db.ExperimentsCache;
import com.rtr.alchemy.db.ExperimentsStore;

import java.util.List;

public class MongoDatabaseProvider implements ExperimentsDatabaseProvider {
    @JsonProperty
    private final List<String> host = Lists.newArrayList("localhost");

    @JsonProperty
    private final int port = 27017;

    @JsonProperty
    private final String db = null;

    public List<String> getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDb() {
        return db;
    }

    @Override
    public ExperimentsStore createStore() {
        return new MongoExperimentsStore();
    }

    @Override
    public ExperimentsCache createCache() {
        return new MongoExperimentsCache();
    }
}