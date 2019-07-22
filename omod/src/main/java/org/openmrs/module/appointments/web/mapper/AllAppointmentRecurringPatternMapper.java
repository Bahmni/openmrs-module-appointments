package org.openmrs.module.appointments.web.mapper;

import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("allAppointmentRecurringPatternMapper")
public class AllAppointmentRecurringPatternMapper extends AbstractAppointmentRecurringPatternMapper {
    @Override
    public AppointmentRecurringPattern fromRequest(AppointmentRequest appointmentRequest) {
        return null;
    }
}
