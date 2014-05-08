package com.rtr.alchemy.service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rtr.alchemy.db.ExperimentsStore;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class ExperimentsStoreConfiguration {
    public abstract ExperimentsStore createStore();
}
