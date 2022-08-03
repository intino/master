package io.intino.test.model;

import io.intino.test.core.TriplesRecord;

import java.time.LocalDate;
import java.util.List;

public class Country extends AbstractCountry {

    protected Country(TriplesRecord record) {
        super(record);
    }

    public boolean isDst(LocalDate date) {
        return hasDst() && isDst(date.toEpochDay());
    }

    private boolean isDst(long day) {
        List<Long> dst = dst();
        return dst.get(0) <= day && day <= dst.get(1);
    }
}
