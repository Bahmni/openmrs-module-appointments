package org.openmrs.module.appointments.web.helper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class RecurringAppointmentsHelper {

    private AppointmentMapper appointmentMapper;

    @Autowired
    public RecurringAppointmentsHelper(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    private final String TIME_FORMAT = "HH:mm:ss";
    private final String DATE_FORMAT = "yyyy-MM-dd";

    public List<Appointment> generateAppointments(List<Date> recurringAppointmentDates,
                                                  AppointmentRequest appointmentRequest) throws ParseException {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String appointmentSlotStartTime = timeFormat.format(appointmentRequest.getStartDateTime());
        String appointmentSlotEndTime = timeFormat.format(appointmentRequest.getEndDateTime());
        List<Appointment> appointments = new ArrayList<>();

        for (Date appointmentDate : recurringAppointmentDates) {
            String formattedDate = dateFormat.format(appointmentDate);
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

            appointment.setStartDateTime(getFormattedDateTime(formattedDate, appointmentSlotStartTime));
            appointment.setEndDateTime(getFormattedDateTime(formattedDate, appointmentSlotEndTime));

            appointments.add(appointment);
        }

        return appointments;
    }

    private Date getFormattedDateTime(String formattedDate, String time) throws ParseException {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT);
        return dateTimeFormat.parse(formattedDate + " " + time);
    }

    public void validateRecurringPattern(RecurringPattern recurringPattern) {
        boolean isValidRecurringPattern = isValidDailyRecurringAppointmentsPattern(recurringPattern);
        if(!isValidRecurringPattern) {
            throw new APIException(String.format("type should be %s/%s\n" +
                    "period and frequency/endDate are mandatory if type is DAY\n" +
                    "daysOfWeek and frequency/endDate are mandatory if type is WEEK",
                    RecurringAppointmentType.DAY, RecurringAppointmentType.WEEK));
        }
    }

    private boolean isValidDailyRecurringAppointmentsPattern(RecurringPattern recurringPattern) {
        return StringUtils.isNotBlank(recurringPattern.getType()) &&
                recurringPattern.getPeriod() > 0 && recurringPattern.getFrequency() > 0;
    }
}
