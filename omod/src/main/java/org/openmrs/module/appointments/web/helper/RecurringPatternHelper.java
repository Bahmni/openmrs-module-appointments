package org.openmrs.module.appointments.web.helper;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.impl.DailyRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecurringPatternHelper {

    private AppointmentMapper appointmentMapper;

    @Autowired
    public RecurringPatternHelper(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    //todo Use strategy for day, week and month logics
    public List<Appointment> generateRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                           AppointmentRequest appointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        try {
            switch (appointmentRecurringPattern.getType()) {
                case WEEK:
                    appointments = new WeeklyRecurringAppointmentsGenerationService().getAppointments(
                            appointmentRecurringPattern, appointmentRequest);
                    break;
                case DAY:
                    appointments = new DailyRecurringAppointmentsGenerationService().getAppointments(
                            appointmentRecurringPattern, appointmentRequest);
                    break;
            }
        } catch (Exception e) {
        }
        return appointments;
    }

}
