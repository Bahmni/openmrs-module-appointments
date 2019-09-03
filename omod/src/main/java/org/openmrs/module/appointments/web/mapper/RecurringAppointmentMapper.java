package org.openmrs.module.appointments.web.mapper;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
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

    public List<AppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments
                .stream()
                .map(appointment -> {
                    AppointmentDefaultResponse appointmentDefaultResponse = appointmentMapper.constructResponse(appointment);
                    appointmentDefaultResponse.setRecurringPattern(
                            recurringPatternMapper.mapToResponse(appointment.getAppointmentRecurringPattern()));
                    return appointmentDefaultResponse;
                }).collect(Collectors.toList());
    }
}
