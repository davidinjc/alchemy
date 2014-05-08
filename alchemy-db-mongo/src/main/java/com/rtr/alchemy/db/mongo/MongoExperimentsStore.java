package com.rtr.alchemy.db.mongo;

import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.Filter;
import com.rtr.alchemy.models.Experiment;

public class MongoExperimentsStore implements ExperimentsStore {
    @Override
    public void save(Experiment experiment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Experiment load(String experimentName, Experiment.Builder builder) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(String experimentName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<Experiment> find(Filter query, Experiment.BuilderFactory factory) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long nextSequenceNumber() {
        return 0;
    }

    @Override
    public Long currentSequenceNumber() {
        return 0L;
    }

    @Override
    public Long sequenceNumber(String experimentName) {
        return 0L;
    }
}
