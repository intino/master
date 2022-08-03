package io.intino.test.model;

import io.intino.test.core.Entity;
import io.intino.test.core.TriplesRecord;

public abstract class Place extends Entity {

    protected Place(TriplesRecord record) {
        super(record);
    }
}
