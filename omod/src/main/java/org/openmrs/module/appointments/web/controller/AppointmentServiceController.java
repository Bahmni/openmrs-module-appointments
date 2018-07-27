package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
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
public class AppointmentServiceController {

    @Autowired
    private AppointmentServiceService appointmentServiceService;
    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET, value = "all/default")
    @ResponseBody
    public List<AppointmentServiceDefaultResponse> getAllAppointmentServices()  {
        List<AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentServices(false);
        List<AppointmentServiceDefaultResponse> response = appointmentServiceMapper.constructDefaultResponseForServiceList(appointmentServices);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "all/full")
    @ResponseBody
    public List<AppointmentServiceFullResponse> getAllAppointmentServicesWithTypes() {
        List<AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentServices(false);
        List<AppointmentServiceFullResponse> response = appointmentServiceMapper.constructFullResponseForServiceList(appointmentServices);
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
    public ResponseEntity<Object> createAppointmentService( @Valid @RequestBody AppointmentServicePayload appointmentServicePayload) throws IOException {
        if(appointmentServicePayload.getName() == null)
            throw new RuntimeException("Appointment Service name should not be null");
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        try {
            AppointmentService savedAppointmentService = appointmentServiceService.save(appointmentService);
            AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(savedAppointmentService);
            return new ResponseEntity<>(appointmentServiceFullResponse, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping( method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> voidAppointmentService(@RequestParam(value = "uuid", required = true) String appointmentServiceUuid, @RequestParam(value = "void_reason", required = false) String voidReason ) {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid);
        if (appointmentService.getVoided()){
            AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentService);
            return new ResponseEntity<>(appointmentServiceFullResponse, HttpStatus.OK);
        }
        try {
            AppointmentService appointmentService1 = appointmentServiceService.voidAppointmentService(appointmentService, voidReason);
            AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentService1);
            return new ResponseEntity<>(appointmentServiceFullResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "load")
    @ResponseBody
    public Integer calculateLoadForService(@RequestParam("uuid") String serviceUuid, @RequestParam(value = "startDateTime") String startDateTime, @RequestParam(value = "endDateTime") String endDateTime)
            throws ParseException {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid(serviceUuid);
        if(appointmentService == null){
            throw new RuntimeException("Appointment Service does not exist");
        }

        return appointmentServiceService.calculateCurrentLoad(appointmentService, DateUtil.convertToLocalDateFromUTC(startDateTime), DateUtil.convertToLocalDateFromUTC(endDateTime));
    }
}
