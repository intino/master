package io.intino.test.model;

import io.intino.test.core.AttributeParser;
import io.intino.test.core.TriplesRecord;

import java.util.List;

public abstract class AbstractCountry extends Place {

    public static Country byId(String id) {
        if(id == null) return null;
        TriplesRecord record = MasterAccessor.get("Country", id);
        return record == null ? null : new Country(record);
    }

    protected AbstractCountry(TriplesRecord record) {
        super(record);
    }

    public String name() {
        return record.get("name");
    }

    public String language() {
        return record.get("language");
    }

    public List<Long> dst() {
        return AttributeParser.toLongList(record.get("dst"));
    }

    public boolean hasDst() {
        return AttributeParser.toBool(record.get("hasDst"));
    }
}
