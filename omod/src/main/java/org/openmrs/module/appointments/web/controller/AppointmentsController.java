package org.openmrs.module.appointments.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearch;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;

import static java.util.Objects.isNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointments")
public class AppointmentsController {
    @Autowired
    private AppointmentsService appointmentsService;
    @Autowired
    private AppointmentMapper appointmentMapper;

    private Log log = LogFactory.getLog(this.getClass());

    @RequestMapping(method = RequestMethod.GET, value="/{uuid}")
    public ResponseEntity<AppointmentDefaultResponse> getAppointmentByUuid(@PathVariable(value = "uuid") String uuid)  {
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        if (appointment == null) {
            log.error("Could not identify appointment with uuid:" + uuid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllAppointments(@RequestParam(value = "forDate", required = false) String forDate) throws ParseException {
        List<Appointment> appointments = appointmentsService.getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate));
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> saveAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest) throws IOException {
        try {
            appointmentRequest.setUuid(null);
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            appointmentsService.validateAndSave(appointment);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
        }catch (RuntimeException e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "search")
    @ResponseBody
    public List<AppointmentDefaultResponse> search(@Valid @RequestBody AppointmentSearch appointmentSearch) {
        List<Appointment> appointments = appointmentsService.search(appointmentSearch);
        if(isNull(appointments)){
            throw new RuntimeException("Either StartDate or EndDate not provided");
        }
        return appointmentMapper.constructResponse(appointments);
    }
}
