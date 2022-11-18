package org.openmrs.module.appointments.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerToAppointmentConverter implements Converter<Integer, Appointment> {
    /**
     * @see Converter#convert(Object)
     */
    @Override
    public Appointment convert(Integer id) {
         AppointmentsService appService = Context.getService(AppointmentsService.class);
       if(id == null){
       return null;
       }
       else{
           return appService.getAppointmentById(id);
       }

    }
}