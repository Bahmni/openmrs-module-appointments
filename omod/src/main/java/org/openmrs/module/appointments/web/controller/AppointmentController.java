package org.openmrs.module.appointments.web.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.*;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllAppointments()  {
        List<Appointment> appointments = appointmentsService.getAllAppointments();
        List<AppointmentDefaultResponse> response = appointmentMapper.constructResponse(appointments);
        return response;
    }
    @RequestMapping( method = RequestMethod.POST, value = "search")
    @ResponseBody
    public List<AppointmentDefaultResponse> searchAppointments( @Valid @RequestBody AppointmentQuery searchQuery) throws IOException {
        Appointment appointment = appointmentMapper.mapQueryToAppointment(searchQuery);
        List<Appointment> appointments =  appointmentsService.Search(appointment);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping( method = RequestMethod.POST)
    @ResponseBody
    public void createAppointment(@Valid @RequestBody AppointmentPayload appointmentPayload) throws IOException {
        if(StringUtils.isBlank(appointmentPayload.getPatientUuid()))
            throw new RuntimeException("Patient should not be empty");
        Appointment appointment = appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        appointmentsService.save(appointment);
    }
}
