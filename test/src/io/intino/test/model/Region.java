package io.intino.test.model;

import io.intino.test.core.TriplesRecord;

import java.time.LocalDate;

public class Region extends AbstractRegion {

    protected Region(TriplesRecord record) {
        super(record);
    }

    public int timeOffsetHours() {
        return (int) timeOffset();
    }

    public int timeOffsetMinutes() {
        double timeOffset = timeOffset();
        return (timeOffset + "").endsWith(".5") ? (timeOffset >= 0 ? 30 : -30) : 0;
    }

    public double timeOffsetOn(LocalDate date) {
        return timeOffset() + dstOn(date);
    }

    private double dstOn(LocalDate date) {
        return hasDst() && country().isDst(date) ? 1 : 0;
    }
}
