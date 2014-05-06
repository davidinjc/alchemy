package com.rtr.alchemy.service;

import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rtr.alchemy.dto.identities.IdentityDto;
import com.rtr.alchemy.identity.Identity;
import com.rtr.alchemy.identity.IdentityType;
import com.rtr.alchemy.mapping.Mapper;
import com.rtr.alchemy.service.config.AlchemyServiceConfiguration;
import com.rtr.alchemy.service.exceptions.RuntimeExceptionMapper;
import com.rtr.alchemy.service.guice.AlchemyModule;
import com.rtr.alchemy.service.health.ExperimentsDatabaseProviderCheck;
import com.rtr.alchemy.service.metadata.IdentitiesMetadata;
import com.rtr.alchemy.service.metadata.IdentityMetadata;
import com.rtr.alchemy.service.metrics.JmxMetricsManaged;
import com.rtr.alchemy.service.resources.ActiveTreatmentsResource;
import com.rtr.alchemy.service.resources.AllocationsResource;
import com.rtr.alchemy.service.resources.ExperimentsResource;
import com.rtr.alchemy.service.resources.MetadataResource;
import com.rtr.alchemy.service.resources.TreatmentOverridesResource;
import com.rtr.alchemy.service.resources.TreatmentsResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * The entry point for the service
 */
public class AlchemyService extends Application<AlchemyServiceConfiguration> {

    private static final Class<?>[] RESOURCES = {
        ExperimentsResource.class,
        AllocationsResource.class,
        TreatmentOverridesResource.class,
        TreatmentsResource.class,
        ActiveTreatmentsResource.class,
        MetadataResource.class
    };

    @Override
    public void initialize(final Bootstrap<AlchemyServiceConfiguration> bootstrap) {
        bootstrap.getObjectMapper().registerModule(new MrBeanModule());
    }

    @Override
    public void run(final AlchemyServiceConfiguration configuration, final Environment environment) throws Exception {
        final IdentitiesMetadata metadata = buildIdentityMetadata(configuration);
        final Injector injector = Guice.createInjector(new AlchemyModule(configuration, environment, metadata));

        for (final Class<?> resource : RESOURCES) {
            environment.jersey().register(injector.getInstance(resource));
        }

        environment.healthChecks().register("database", injector.getInstance(ExperimentsDatabaseProviderCheck.class));
        environment.jersey().register(new RuntimeExceptionMapper());
        environment.lifecycle().manage(new JmxMetricsManaged(environment));
        registerIdentitySubTypes(configuration, environment);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> getClass(String name, Class<T> clazz) throws ClassNotFoundException {
        final Class<?> genericClass = Class.forName(name);
        if (!clazz.isAssignableFrom(genericClass)) {
            throw new ClassCastException(String.format("%s is not of expected type %s", name, clazz));
        }

        return (Class<? extends T>) genericClass;
    }

    private IdentitiesMetadata buildIdentityMetadata(AlchemyServiceConfiguration configuration) {
        final IdentitiesMetadata metadata = new IdentitiesMetadata();

        for (final Class<? extends Identity> identityType : configuration.getIdentities()) {
            final String canonicalName = identityType.getCanonicalName();
            final String identityDtoClassName = String.format("%sDto", identityType.getCanonicalName());
            final Class<? extends IdentityDto> identityDtoType;

            try {
                identityDtoType = getClass(identityDtoClassName, IdentityDto.class);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(
                    String.format(
                        "could not find corresponding DTO type %s for %s, ensure that annotation processor was executed " +
                            "and that generated sources are in the classpath",
                        identityDtoClassName,
                        canonicalName
                    )
                );
            }

            final String identityMapperClassName = String.format("%sMapper", identityType.getCanonicalName());
            final Class<? extends Mapper> identityMapperType;

            try {
                identityMapperType = getClass(identityMapperClassName, Mapper.class);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(
                    String.format(
                        "could not find corresponding Mapper type %s for %s, ensure that annotation processor was executed " +
                            "and that generated sources are in the classpath",
                        identityMapperClassName,
                        canonicalName
                    )
                );
            }

            final IdentityType identityTypeAnnotation = identityType.getAnnotation(IdentityType.class);
            if (identityTypeAnnotation == null) {
                throw new IllegalStateException(
                    String.format("type %s does not specify the IdentityType annotation", canonicalName)
                );
            }

            metadata.put(
                identityTypeAnnotation.value(),
                new IdentityMetadata(
                    identityTypeAnnotation.value(),
                    identityType,
                    identityDtoType,
                    identityMapperType
                )
            );
        }

        return metadata;
    }

    private void registerIdentitySubTypes(AlchemyServiceConfiguration configuration, Environment environment) {
        for (final Class<?> identityType : configuration.getIdentities()) {
            final String identityDtoClassName = String.format("%sDto", identityType.getCanonicalName());
            final Class<?> identityDtoType;

            try {
                identityDtoType = Class.forName(identityDtoClassName);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(
                    String.format(
                        "Could not find corresponding DTO type %s for %s, ensure that annotation processor was executed " +
                            "and that generated sources are in the classpath",
                        identityDtoClassName,
                        identityType.getCanonicalName()
                    )
                );
            }

            environment.getObjectMapper().registerSubtypes(identityDtoType);
        }
    }

    public static void main(final String[] args) throws Exception {
        new AlchemyService().run(args);
    }
}
