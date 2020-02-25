package org.openmrs.module.appointments.web.contract;

import java.util.Date;
import java.util.List;

public class RecurringPattern {

    private Integer id;
    private Integer frequency;
    private int period;
    private Date endDate;
    private String type;
    private List<String> daysOfWeek;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public boolean isFrequencyIncreased(Integer frequency) {
        return this.getFrequency() > frequency;
    }

    public boolean isFrequencyDecreased(Integer frequency) {
        return this.getFrequency() < frequency;
    }

    public boolean isAfter(Date endDate) {
        return this.getEndDate().after(endDate);
    }

    public boolean isBefore(Date endDate) {
        return this.getEndDate().before(endDate);
    }

    @Override
    public String toString() {
        return "RecurringPattern{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", period=" + period +
                ", endDate=" + endDate +
                ", type='" + type + '\'' +
                ", daysOfWeek=" + daysOfWeek +
                '}';
    }
}
