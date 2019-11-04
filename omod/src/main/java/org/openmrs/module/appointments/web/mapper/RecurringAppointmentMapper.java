package org.openmrs.module.appointments.web.mapper;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentDefaultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecurringAppointmentMapper {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RecurringPatternMapper recurringPatternMapper;

    public List<RecurringAppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments
                .stream()
                .map(this::constructResponse).collect(Collectors.toList());
    }

    // TODO Need tests
    public RecurringAppointmentDefaultResponse constructResponse(Appointment appointment) {
        RecurringAppointmentDefaultResponse recurringAppointmentDefaultResponse =
                            new RecurringAppointmentDefaultResponse();
        AppointmentDefaultResponse appointmentDefaultResponse = appointmentMapper.constructResponse(appointment);
        recurringAppointmentDefaultResponse.setAppointmentDefaultResponse(appointmentDefaultResponse);
        recurringAppointmentDefaultResponse.setRecurringPattern(
                recurringPatternMapper.mapToResponse(appointment.getAppointmentRecurringPattern()));
        return recurringAppointmentDefaultResponse;
    }
}
