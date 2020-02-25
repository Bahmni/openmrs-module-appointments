package org.openmrs.module.appointments.web.util;

import org.openmrs.module.appointments.web.contract.RecurringPattern;

import java.util.Date;
import java.util.List;

public class RecurringPatternBuilder {

    private RecurringPattern recurringPattern;

    public RecurringPatternBuilder() {
        recurringPattern = new RecurringPattern();
    }

    public RecurringPattern get() {
        return recurringPattern;
    }

    public RecurringPatternBuilder setId(Integer id) {
        recurringPattern.setId(id);
        return this;
    }

    public RecurringPatternBuilder setFrequency(Integer frequency) {
        recurringPattern.setFrequency(frequency);
        return this;
    }

    public RecurringPatternBuilder setPeriod(int period) {
        recurringPattern.setPeriod(period);
        return this;
    }


    public RecurringPatternBuilder setEndDate(Date endDate) {
        recurringPattern.setEndDate(endDate);
        return this;
    }


    public RecurringPatternBuilder setType(String type) {
        recurringPattern.setType(type);
        return this;
    }

    public RecurringPatternBuilder setDaysOfWeek(List<String> daysOfWeek) {
        recurringPattern.setDaysOfWeek(daysOfWeek);
        return this;
    }


}
