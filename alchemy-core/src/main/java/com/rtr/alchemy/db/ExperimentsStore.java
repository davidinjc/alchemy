package com.rtr.alchemy.db;

import com.rtr.alchemy.models.Experiment;

import java.io.Closeable;

/**
 * An interface for defining basic CRUD operations around experiments, treatments and allocations.  These operations do
 * not need to be highly optimized or fast
 */
public interface ExperimentsStore extends Closeable {
    /**
     * Save an experiment, creating or updating it
     * @param experiment The experiment to create or update
     */
    void save(Experiment experiment);

    /**
     * Retrieves an experiment
     * @param experimentName The name of the experiment
     * @param builder The builder to use to construct the experiment
     * @return The experiment with the given name
     */
    Experiment load(String experimentName, Experiment.Builder builder);

    /**
     * Deletes an experiment and its associated data
     * @param experimentName The name of the experiment
     */
    void delete(String experimentName);

    /**
     * Finds experiments with given criteria
     * @param query Criteria for pagination and filtering
     * @param factory a builder factory for creating new instances of Experiment
     * @return Filtered list of experiments
     */
    Iterable<Experiment> find(Query query, Experiment.BuilderFactory factory);

    /**
     * Requests a unique sequence number for an experiment, which must always increment.  This is used
     * for versioning for caching
     */
    long nextSequenceNumber();

    /**
     * Retrieves the last generated sequence number or null if no sequence number has been generated yet
     */
    Long currentSequenceNumber();

    /**
     * Retrieves sequence number for a given experiment
     * @return the sequence number or null if experiment does not exist
     */
    Long sequenceNumber(String experimentName);
}
