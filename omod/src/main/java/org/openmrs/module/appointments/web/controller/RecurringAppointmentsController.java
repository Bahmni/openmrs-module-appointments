package org.openmrs.module.appointments.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.helper.RecurringPatternHelper;
import org.openmrs.module.appointments.web.mapper.AbstractAppointmentRecurringPatternMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/recurring-appointments")
public class RecurringAppointmentsController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Autowired
    private RecurringPatternHelper recurringPatternHelper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    @Qualifier("allAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper allAppointmentRecurringPatternMapper;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            RecurringPattern recurringPattern = appointmentRequest.getRecurringPattern();
            recurringPatternHelper.validateRecurringPattern(recurringPattern);
            AppointmentRecurringPattern appointmentRecurringPattern = allAppointmentRecurringPatternMapper.fromRequest(recurringPattern);
            List<Appointment> appointmentsList = recurringPatternHelper.generateRecurringAppointments(appointmentRecurringPattern,
                    appointmentRequest);
            appointmentRecurringPattern.setAppointments(new HashSet<>(appointmentsList));
            appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern);
            return new ResponseEntity<>(appointmentMapper.constructResponse(
                    new ArrayList<>(appointmentRecurringPattern.getAppointments())), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to create recurring appointments", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


}
