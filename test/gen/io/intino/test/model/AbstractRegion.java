package io.intino.test.model;

import io.intino.test.core.AttributeParser;
import io.intino.test.core.TriplesRecord;

public class AbstractRegion extends Place {

    public static Region byId(String id) {
        if(id == null) return null;
        TriplesRecord record = MasterAccessor.get("Region", id);
        return record == null ? null : new Region(record);
    }

    protected AbstractRegion(TriplesRecord record) {
        super(record);
    }

    public String name() {
        return record.get("name");
    }

    public Country country() {
        return Country.byId(record.get("country"));
    }

    public double timeOffset() {
        return AttributeParser.toDecimal(record.get("timeOffset"));
    }

    public boolean hasDst() {
        return AttributeParser.toBool(record.get("hasDst"));
    }

}
