package org.openmrs.module.appointments.web.mapper;

import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("singleAppointmentRecurringPatternMapper")
public class SingleAppointmentRecurringPatternMapper extends AbstractAppointmentRecurringPatternMapper {

    @Override
    public AppointmentRecurringPattern fromRequest(AppointmentRequest appointmentRequest){
        return null;
    }
}
