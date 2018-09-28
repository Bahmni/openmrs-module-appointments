package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointment-services")
public class AppointmentServicesController extends BaseRestController {

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET, value="/{uuid}")
    @ResponseBody
    public AppointmentServiceFullResponse getAppointmentServiceByUuid(@PathVariable("uuid") String uuid)  {
        AppointmentServiceDefinition appointmentServiceDef = appointmentServiceDefinitionService.getAppointmentServiceByUuid(uuid);
        if (appointmentServiceDef == null) {
            throw new RuntimeException("Appointment Service does not exist");
        }
        AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentServiceDef);
        return appointmentServiceFullResponse;
    }
}
