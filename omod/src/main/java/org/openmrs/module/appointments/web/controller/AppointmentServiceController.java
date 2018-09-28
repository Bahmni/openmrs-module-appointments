package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDescription;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentService")
public class AppointmentServiceController extends BaseRestController {

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;
    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET, value = "all/default")
    @ResponseBody
    public List<AppointmentServiceDefaultResponse> getAllAppointmentServices()  {
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = appointmentServiceDefinitionService.getAllAppointmentServices(false);
        List<AppointmentServiceDefaultResponse> response = appointmentServiceMapper.constructDefaultResponseForServiceList(appointmentServiceDefinitions);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "all/full")
    @ResponseBody
    public List<AppointmentServiceFullResponse> getAllAppointmentServicesWithTypes() {
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = appointmentServiceDefinitionService.getAllAppointmentServices(false);
        List<AppointmentServiceFullResponse> response = appointmentServiceMapper.constructFullResponseForServiceList(appointmentServiceDefinitions);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppointmentServiceFullResponse getAppointmentServiceByUuid(@RequestParam("uuid") String uuid)  {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid(uuid);
        if(appointmentServiceDefinition == null){
            throw new RuntimeException("Appointment Service does not exist");
        }
        AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentServiceDefinition);

        return appointmentServiceFullResponse;
    }

    @RequestMapping( method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> defineAppointmentService(@Valid @RequestBody AppointmentServiceDescription appointmentServiceDescription) throws IOException {
        if(appointmentServiceDescription.getName() == null)
            throw new RuntimeException("Appointment Service name should not be null");
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        try {
            AppointmentServiceDefinition savedAppointmentServiceDefinition = appointmentServiceDefinitionService.save(appointmentServiceDefinition);
            AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(savedAppointmentServiceDefinition);
            return new ResponseEntity<>(appointmentServiceFullResponse, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping( method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> voidAppointmentService(@RequestParam(value = "uuid", required = true) String appointmentServiceUuid, @RequestParam(value = "void_reason", required = false) String voidReason ) {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid(appointmentServiceUuid);
        if (appointmentServiceDefinition.getVoided()){
            AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentServiceDefinition);
            return new ResponseEntity<>(appointmentServiceFullResponse, HttpStatus.OK);
        }
        try {
            AppointmentServiceDefinition appointmentServiceDefinition1 = appointmentServiceDefinitionService.voidAppointmentService(appointmentServiceDefinition, voidReason);
            AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentServiceDefinition1);
            return new ResponseEntity<>(appointmentServiceFullResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "load")
    @ResponseBody
    public Integer calculateLoadForService(@RequestParam("uuid") String serviceUuid, @RequestParam(value = "startDateTime") String startDateTime, @RequestParam(value = "endDateTime") String endDateTime)
            throws ParseException {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid(serviceUuid);
        if(appointmentServiceDefinition == null){
            throw new RuntimeException("Appointment Service does not exist");
        }

        return appointmentServiceDefinitionService.calculateCurrentLoad(appointmentServiceDefinition, DateUtil.convertToLocalDateFromUTC(startDateTime), DateUtil.convertToLocalDateFromUTC(endDateTime));
    }
}
