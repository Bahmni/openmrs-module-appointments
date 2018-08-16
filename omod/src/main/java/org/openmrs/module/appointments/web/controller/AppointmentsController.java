package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointments")
public class AppointmentsController {
    @Autowired
    private AppointmentsService appointmentsService;
    @Autowired
    private AppointmentMapper appointmentMapper;

    @RequestMapping(method = RequestMethod.GET, value="/{uuid}")
    @ResponseBody
    public AppointmentDefaultResponse getAppointmentByUuid(@PathVariable(value = "uuid") String uuid)  {
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        if (appointment == null) {
            throw new RuntimeException("Appointment does not exist");
        }
        return appointmentMapper.constructResponse(appointment);
    }

}
