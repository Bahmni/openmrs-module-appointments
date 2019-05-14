package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.RecurringAppointmentService;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Transactional
public class RecurringAppointmentServiceImpl implements RecurringAppointmentService {


    private final String DATE_FORMAT = "yyyy-MM-dd";

    AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    AppointmentsService appointmentsService;

    public void setAppointmentRecurringPatternDao(AppointmentRecurringPatternDao appointmentRecurringPatternDao) {
        this.appointmentRecurringPatternDao = appointmentRecurringPatternDao;
    }

    public void setAppointmentsService(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public List<Appointment> saveRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                       List<Appointment> appointments) {

        appointments.forEach(appointment -> appointmentsService.validateAndSave(appointment));
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return appointments;

    }

    @Override
    public List<Date> getRecurringDates(Date appointmentStartDateTime,
                                        AppointmentRecurringPattern appointmentRecurringPattern) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentStartDateTime);
        List<Date> appointmentDates = new ArrayList<>();
        Date endDate = null;

        if (appointmentRecurringPattern.getEndDate() == null) {
            switch (appointmentRecurringPattern.getType().toUpperCase()) {
                case "DAY":
                default:
                    endDate = getEndDateForDayType(calendar, appointmentRecurringPattern);
                    break;
            }
        } else {
            endDate = appointmentRecurringPattern.getEndDate();
        }

        Date currentAppointmentDate = dateFormat.parse(dateFormat.format(appointmentStartDateTime));

        while (!currentAppointmentDate.after(endDate)) {
            appointmentDates.add(currentAppointmentDate);

            calendar = Calendar.getInstance();
            calendar.setTime(currentAppointmentDate);
            calendar.add(Calendar.DAY_OF_MONTH, appointmentRecurringPattern.getPeriod());
            currentAppointmentDate = calendar.getTime();
        }
        return appointmentDates;
    }


    private Date getEndDateForDayType(Calendar calendar, AppointmentRecurringPattern recurringPattern) {
        calendar.add(Calendar.DAY_OF_MONTH, recurringPattern.getPeriod() * (recurringPattern.getFrequency() - 1));
        return calendar.getTime();
    }

}
