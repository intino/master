package io.intino.test.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class TriplesRecord implements Serializable {

    private final String id;
    private final Map<String, String> attributes = new HashMap<>();

    public TriplesRecord(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String get(String attribute) {
        return attributes.get(attribute);
    }

    public int size() {
        return attributes.size();
    }

    public void put(String key, String value) {
        attributes.put(key, value);
    }
}
