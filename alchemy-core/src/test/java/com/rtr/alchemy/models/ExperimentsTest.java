package com.rtr.alchemy.models;

import com.google.common.collect.Lists;
import com.rtr.alchemy.caching.NoOpRefreshStrategy;
import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.FilterableFields;
import com.rtr.alchemy.db.Query;
import com.rtr.alchemy.identities.Identity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import static org.junit.Assert.assertEquals;

public class ExperimentsTest {
    private ExperimentsStore store;
    private Experiments experiments;
    private Experiment experiment;

    @Before
    public void setUp() {
        store = mock(ExperimentsStore.class);

        experiment = mock(Experiment.class);
        doReturn("foo").when(experiment).getName();
        doReturn(Lists.newArrayList(experiment))
            .when(store)
            .find(any(Query.class), any(Experiment.BuilderFactory.class));

        // will invalidateAll and load from store
        experiments =
            Experiments
                .using(store)
                .using(new NoOpRefreshStrategy())
                .build();
        verify(store).find(any(Query.class), any(Experiment.BuilderFactory.class));

        // reset
        reset(store);
        doReturn(Lists.newArrayList(experiment))
            .when(store)
            .find(any(Query.class), any(Experiment.BuilderFactory.class));
    }


    @Test
    public void testGetActiveTreatment() {
        final Identity identity = mock(Identity.class);
        experiments.getActiveTreatment("foo", identity);
        verifyZeroInteractions(store);
        experiments.getCache().invalidateAll(false);
        verify(store).find(any(Query.class), any(Experiment.BuilderFactory.class));
        verify(experiment).getOverride(eq(identity));
    }

    @Test
    public void testGetActiveTreatments() {
        final Identity identity = mock(Identity.class);
        experiments.getActiveTreatments(identity);
        verifyZeroInteractions(store);
        experiments.getCache().invalidateAll(false);
        experiments.getActiveTreatments(identity);
        verify(store).find(any(Query.class), any(Experiment.BuilderFactory.class));
        verify(experiment, times(2)).getOverride(eq(identity));
    }

    @Test
    public void testGetActiveExperiments() {
        experiments.getCache().getActiveExperiments();
        verifyZeroInteractions(store);
    }

    @Test
    public void testCreate() {
        experiments.create("foo").save();
        verify(store).save(any(Experiment.class));
    }

    @Test
    public void testSave() {
        final Experiment experiment = experiments.create("foo").save();
        verify(store).save(eq(experiment));
    }

    @Test
    public void testFind() {
        experiments.find();
        final ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(store).find(query.capture(), any(Experiment.BuilderFactory.class));

        assertEquals(0, query.getValue().getFilters().size());
        assertEquals(0, query.getValue().getOrderings().size());
        assertNull(query.getValue().getLimit());
        assertNull(query.getValue().getOffset());
    }

    @Test
    public void testFindFiltered() {
        final Query query =
            Query
                .criteria()
                .filter(FilterableFields.NAME, "foo")
                .offset(1)
                .limit(2)
                .build();
        experiments.find(query);
        verify(store).find(eq(query), any(Experiment.BuilderFactory.class));
    }

    @Test
    public void testDelete() {
        experiments.delete("foo");
        verify(store).delete(eq("foo"));
    }
}
