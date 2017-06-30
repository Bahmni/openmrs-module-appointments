package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.contract.AppointmentServiceResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentService")
public class AppointmentServiceController {

    @Autowired
    private AppointmentServiceService appointmentServiceService;
    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List<AppointmentServiceResponse> getAllAppointmentServices()  {
        List<AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentServices();
        List<AppointmentServiceResponse> response = appointmentServiceMapper.constructResponse(appointmentServices);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppointmentServiceResponse getAppointmentServiceByUuid(@RequestParam("uuid") String uuid)  {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid(uuid);
        if(appointmentService == null){
            throw new RuntimeException("Appointment Service does not exist");
        }
        AppointmentServiceResponse appointmentServiceResponse = appointmentServiceMapper.constructResponse(appointmentService);

        return appointmentServiceResponse;
    }

    @RequestMapping( method = RequestMethod.POST)
    @ResponseBody
    public void createAppointmentService( @Valid @RequestBody AppointmentServicePayload appointmentServicePayload) throws IOException {
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        appointmentServiceService.save(appointmentService);
    }

}
