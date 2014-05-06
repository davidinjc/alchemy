package com.rtr.alchemy.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Sets;
import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.service.jackson.ClassKeyDeserializer;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * The base configuration for the Alchemy Dropwizard service
 */
public class AlchemyServiceConfiguration extends Configuration {
    /**
     * Defines a list of known identity types, which are used for assigning users to a treatment
     */
    @SuppressWarnings("unchecked")
    @JsonDeserialize(keyUsing = ClassKeyDeserializer.class)
    private final Set<Class<? extends Identity>> identities = Sets.newHashSet();

    public Set<Class<? extends Identity>> getIdentities() {
        return identities;
    }

    @JsonProperty
    @NotNull
    private final DatabaseProviderConfiguration provider = null;
    public DatabaseProviderConfiguration getProvider() {
        return provider;
    }
}
