package io.intino.test.core;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class Entity {

    protected final TriplesRecord record;

    protected Entity(TriplesRecord record) {
        this.record = requireNonNull(record);
    }

    public String id() {
        return record.id();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id(), entity.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id());
    }

    @Override
    public String toString() {
        return id();
    }
}
