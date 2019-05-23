package org.openmrs.module.appointments.web.helper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class RecurringAppointmentsHelper {

    private AppointmentMapper appointmentMapper;

    @Autowired
    public RecurringAppointmentsHelper(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    public void validateRecurringPattern(RecurringPattern recurringPattern) {
        boolean isValidRecurringPattern = isValidDailyRecurringAppointmentsPattern(recurringPattern);
        if (!isValidRecurringPattern) {
            throw new APIException(String.format("type should be %s/%s\n" +
                            "period and frequency/endDate are mandatory if type is DAY\n" +
                            "daysOfWeek and frequency/endDate are mandatory if type is WEEK",
                    RecurringAppointmentType.DAY, RecurringAppointmentType.WEEK));
        }
    }

    private boolean isValidDailyRecurringAppointmentsPattern(RecurringPattern recurringPattern) {
        return StringUtils.isNotBlank(recurringPattern.getType()) &&
                recurringPattern.getPeriod() > 0 && (hasValidFrequencyOrEndDate(recurringPattern.getFrequency(), recurringPattern.getEndDate()));
    }

    private boolean hasValidFrequencyOrEndDate(int frequency, Date endDate) {
        return frequency > 0 || endDate != null;
    }

    //todo Use strategy for day, week and month logics
    public List<Appointment> generateRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern, AppointmentRequest appointmentRequest) {
        List<Appointment> appointments;
        Date endDate;
        if (appointmentRecurringPattern.getEndDate() == null) {
            switch (appointmentRecurringPattern.getType()) {
                case DAY:
                default:
                    endDate = getEndDateForDayType(appointmentRequest.getStartDateTime(), appointmentRecurringPattern);
                    break;
            }
        } else {
            endDate = appointmentRecurringPattern.getEndDate();
        }

        appointments = generateAppointmentsForDayType(appointmentRecurringPattern, appointmentRequest, endDate);
        return appointments;
    }

    private List<Appointment> generateAppointmentsForDayType(AppointmentRecurringPattern appointmentRecurringPattern, AppointmentRequest appointmentRequest, Date endDate) {
        List<Appointment> appointments = new ArrayList<>();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        Date currentAppointmentDate = appointmentRequest.getStartDateTime();
        while (!currentAppointmentDate.after(endDate)) {
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            appointment.setStartDateTime(startCalender.getTime());
            appointment.setEndDateTime(endCalender.getTime());
            startCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
            endCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
            appointments.add(appointment);
            currentAppointmentDate = startCalender.getTime();
        }
        return appointments;
    }

    private Date getEndDateForDayType(Date startDate, AppointmentRecurringPattern recurringPattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, recurringPattern.getPeriod() * (recurringPattern.getFrequency() - 1));
        return calendar.getTime();
    }
}
