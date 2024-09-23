package com.kamarkaka.moneytracker.db;

import java.time.LocalDate;

public class DateRange {
    private final LocalDate beginDate;
    private final LocalDate endDate;

    public DateRange(LocalDate beginDate, LocalDate endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
