package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentService")
public class AppointmentServiceController {

    @Autowired
    private AppointmentServiceService appointmentServiceService;
    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List<AppointmentServiceDefaultResponse> getAllAppointmentServices()  {
        List<AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentServices(false);
        List<AppointmentServiceDefaultResponse> response = appointmentServiceMapper.constructResponse(appointmentServices);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppointmentServiceFullResponse getAppointmentServiceByUuid(@RequestParam("uuid") String uuid)  {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid(uuid);
        if(appointmentService == null){
            throw new RuntimeException("Appointment Service does not exist");
        }
        AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentService);

        return appointmentServiceFullResponse;
    }

    @RequestMapping( method = RequestMethod.POST)
    @ResponseBody
    public AppointmentServiceFullResponse createAppointmentService( @Valid @RequestBody AppointmentServicePayload appointmentServicePayload) throws IOException {
        if(appointmentServicePayload.getName() == null)
            throw new RuntimeException("Appointment Service name should not be null");
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        AppointmentService savedAppointmentService = appointmentServiceService.save(appointmentService);
        return appointmentServiceMapper.constructResponse(savedAppointmentService);
    }
}
