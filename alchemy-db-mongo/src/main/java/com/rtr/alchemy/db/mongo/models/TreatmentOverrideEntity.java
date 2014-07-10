package com.rtr.alchemy.db.mongo.models;

import com.rtr.alchemy.models.TreatmentOverride;
import org.mongodb.morphia.annotations.Embedded;

import javax.annotation.Nullable;

/**
 * An entity that mirrors TreatmentOverride
 * @see com.rtr.alchemy.models.TreatmentOverride
 */
@Embedded
public class TreatmentOverrideEntity {
    public String name;
    public String treatment;
    public String filter;

    public static TreatmentOverrideEntity from(TreatmentOverride override) {
        return new TreatmentOverrideEntity(override);
    }

    // Required by Morphia
    private TreatmentOverrideEntity() { }

    private TreatmentOverrideEntity(TreatmentOverride override) {
        this.name = override.getName();
        this.treatment = override.getTreatment().getName();
        this.filter = override.getFilter().toString();
    }
}