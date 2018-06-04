package com.myserialization.custom.wrapper;

import java.time.LocalDate;

public class LocalDateWrapper implements ObjectWrapper {

    private int year;
    private int month;
    private int day;

    public LocalDateWrapper() {
    }

    @Override
    public LocalDate readObject() {
        return LocalDate.of(year,month,day) ;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.year = ((LocalDate)o).getYear();
        this.month = ((LocalDate)o).getMonthValue();
        this.day = ((LocalDate)o).getDayOfMonth();
        return this;
    }
}
