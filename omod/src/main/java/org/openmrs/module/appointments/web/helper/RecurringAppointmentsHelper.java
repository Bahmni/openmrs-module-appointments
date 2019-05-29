package org.openmrs.module.appointments.web.helper;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.impl.DailyRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.valueOf;

@Component
public class RecurringAppointmentsHelper {

    private AppointmentMapper appointmentMapper;

    @Autowired
    public RecurringAppointmentsHelper(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    public boolean validateRecurringPattern(RecurringPattern recurringPattern) {
        boolean isValidRecurringPattern = isValidRecurringAppointmentsPattern(recurringPattern);
        if (!isValidRecurringPattern) {
            throw new APIException(String.format("type should be %s/%s\n" +
                            "period and frequency/endDate are mandatory if type is DAY\n" +
                            "daysOfWeek, period and frequency/endDate are mandatory if type is WEEK",
                    RecurringAppointmentType.DAY, RecurringAppointmentType.WEEK));
        }
        return isValidRecurringPattern;
    }

    private boolean isValidRecurringAppointmentsPattern(RecurringPattern recurringPattern) {
        boolean isValidRecurringPattern = false;
        try {
            switch (valueOf(recurringPattern.getType().toUpperCase())) {
                case WEEK:
                    isValidRecurringPattern = isValidWeeklyRecurringAppointmentsPattern(recurringPattern);
                    break;
                case DAY:
                    isValidRecurringPattern = isValidDailyRecurringAppointmentsPattern(recurringPattern);
                    break;
            }
        } catch (Exception e) {
        }
        return isValidRecurringPattern;
    }

    private boolean isValidDailyRecurringAppointmentsPattern(RecurringPattern recurringPattern) {
        return recurringPattern.getPeriod() > 0
                && (hasValidFrequencyOrEndDate(recurringPattern.getFrequency(), recurringPattern.getEndDate()));
    }

    private boolean isValidWeeklyRecurringAppointmentsPattern(RecurringPattern recurringPattern) {
        return areValidDays(recurringPattern.getDaysOfWeek())
                && isValidDailyRecurringAppointmentsPattern(recurringPattern);
    }

    private boolean areValidDays(List<String> daysOfWeek) {
        List<String> WEEK_DAYS = Arrays.asList("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");
        return CollectionUtils.isNotEmpty(daysOfWeek)
                && WEEK_DAYS.containsAll(daysOfWeek.stream().map(String::toUpperCase).collect(Collectors.toList()));
    }

    private boolean hasValidFrequencyOrEndDate(int frequency, Date endDate) {
        return frequency > 0 || endDate != null;
    }

    //todo Use strategy for day, week and month logics
    public List<Appointment> generateRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                           AppointmentRequest appointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        try {
            switch (appointmentRecurringPattern.getType()) {
                case WEEK:
                    appointments = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                            appointmentRequest, appointmentMapper).getAppointments();
                    break;
                case DAY:

                    appointments = new DailyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                            appointmentRequest, appointmentMapper).getAppointments();
                    break;
            }
        } catch (Exception e) {
        }
        return appointments;
    }

}
