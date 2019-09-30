package org.openmrs.module.appointments.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringAppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.service.impl.AllAppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.openmrs.module.appointments.web.service.impl.SingleAppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.validators.RecurringPatternValidator;
import org.openmrs.module.appointments.web.validators.TimeZoneValidator;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/recurring-appointments")
public class RecurringAppointmentsController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Autowired
    private RecurringPatternValidator recurringPatternValidator;

    @Autowired
    private RecurringAppointmentsService recurringAppointmentsService;

    @Autowired
    private RecurringAppointmentMapper recurringAppointmentMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RecurringPatternMapper recurringPatternMapper;

    @Autowired
    private SingleAppointmentRecurringPatternUpdateService singleAppointmentRecurringPatternUpdateService;

    @Autowired
    private AllAppointmentRecurringPatternUpdateService allAppointmentRecurringPatternUpdateService;

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private TimeZoneValidator timeZoneValidator;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody RecurringAppointmentRequest recurringAppointmentRequest) {
        try {
            RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
            Errors errors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");
            recurringPatternValidator.validate(recurringPattern, errors);
            if (!errors.getAllErrors().isEmpty()) {
                throw new APIException(errors.getAllErrors().get(0).getDefaultMessage());
            }
            AppointmentRecurringPattern appointmentRecurringPattern = recurringPatternMapper.fromRequest(recurringPattern);
            List<Appointment> appointmentsList = recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest);
            appointmentRecurringPattern.setAppointments(new HashSet<>(appointmentsList));
            appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern);
            return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(
                    new ArrayList<>(appointmentRecurringPattern.getAppointments())), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to create recurring appointments", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{appointmentUuid}/changeStatus")
    @ResponseBody
    public ResponseEntity<Object> transitionAppointment(@PathVariable("appointmentUuid") String appointmentUuid, @RequestBody Map<String, String> statusDetails) throws ParseException {
        try {
            String clientTimeZone = statusDetails.get("timeZone");
            Errors errors = new BeanPropertyBindingResult(clientTimeZone, "clientTimeZone");
            timeZoneValidator.validate(clientTimeZone, errors);
            if (!errors.getAllErrors().isEmpty()) {
                throw new APIException(errors.getAllErrors().get(0).getDefaultMessage());
            }

            String toStatus = statusDetails.get("toStatus");
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if (appointment != null) {
                List<Appointment> appointments = appointmentRecurringPatternService.changeStatus(appointment, toStatus, clientTimeZone);
                return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(appointments), HttpStatus.OK);
            } else {
                throw new RuntimeException("Appointment does not exist");
            }
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to validateAndUpdate appointment status", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{appointmentUuid}")
    @ResponseBody
    public ResponseEntity<Object> editAppointment(@Valid @RequestBody RecurringAppointmentRequest recurringAppointmentRequest) {
        try {
            RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
            Errors recurringPatternErrors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");
            recurringPatternValidator.validate(recurringPattern, recurringPatternErrors);
            if (!recurringPatternErrors.getAllErrors().isEmpty()) {
                throw new APIException(recurringPatternErrors.getAllErrors().get(0).getDefaultMessage());
            }
            if (recurringAppointmentRequest.getApplyForAll()) {
                String clientTimeZone = recurringAppointmentRequest.getTimeZone();
                Errors timeZoneErrors = new BeanPropertyBindingResult(clientTimeZone, "clientTimeZone");
                timeZoneValidator.validate(clientTimeZone, timeZoneErrors);
                if (!timeZoneErrors.getAllErrors().isEmpty()) {
                    throw new APIException(timeZoneErrors.getAllErrors().get(0).getDefaultMessage());
                }
                AppointmentRecurringPattern appointmentRecurringPattern = allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);
                Appointment editedAppointment = appointmentRecurringPattern.getAppointments().stream()
                        .filter(app -> recurringAppointmentRequest.getAppointmentRequest().getUuid().equals(app.getUuid())).findFirst().orElse(null);
                AppointmentRecurringPattern updatedAppointmentRecurringPattern = appointmentRecurringPatternService.update(appointmentRecurringPattern, editedAppointment);
                List<Appointment> updatedAppointments = new ArrayList<>(updatedAppointmentRecurringPattern.getAppointments());
                return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(updatedAppointments), HttpStatus.OK);
            } else {
                Appointment appointmentToBeUpdated = singleAppointmentRecurringPatternUpdateService.getUpdatedAppointment(recurringAppointmentRequest);
                Appointment updatedAppointment = appointmentRecurringPatternService.update(appointmentToBeUpdated.getAppointmentRecurringPattern(),
                        Arrays.asList(appointmentToBeUpdated, appointmentToBeUpdated.getRelatedAppointment()));
                return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(Arrays.asList(updatedAppointment)), HttpStatus.OK);
            }
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to validateAndUpdate an appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public RecurringAppointmentDefaultResponse getAppointmentByUuid(@RequestParam(value = "uuid") String uuid)  {
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        if(appointment == null) {
            log.error("Invalid. Appointment does not exist. UUID - " + uuid);
            throw new RuntimeException("Appointment does not exist");
        }
        return recurringAppointmentMapper.constructResponse(appointment);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/conflicts")
    @ResponseBody
    public ResponseEntity<Object> getConflicts(@RequestBody RecurringAppointmentRequest recurringAppointmentRequest) {
        try {
            RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
            Errors errors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");
            recurringPatternValidator.validate(recurringPattern, errors);
            if (!errors.getAllErrors().isEmpty()) {
                throw new APIException(errors.getAllErrors().get(0).getDefaultMessage());
            }
            String clientTimeZone = recurringAppointmentRequest.getTimeZone();
            Errors timeZoneErrors = new BeanPropertyBindingResult(clientTimeZone, "clientTimeZone");
            timeZoneValidator.validate(clientTimeZone, timeZoneErrors);
            if (!timeZoneErrors.getAllErrors().isEmpty()) {
                throw new APIException(timeZoneErrors.getAllErrors().get(0).getDefaultMessage());
            }
            List<Appointment> appointments = getValidAppointments(recurringAppointmentRequest);
            Map<String, List<Appointment>> appointmentsConflicts = appointmentsService.getAppointmentsConflicts(appointments);
            if (appointmentsConflicts.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(appointmentMapper.constructConflictResponse(appointmentsConflicts),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to get getConflicts for recurring appointments", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Appointment> getValidAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments;
        if (Objects.isNull(recurringAppointmentRequest.getAppointmentRequest().getUuid())) {
            appointments = recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest);
        } else {
            AppointmentRecurringPattern appointmentRecurringPattern = allAppointmentRecurringPatternUpdateService
                    .getUpdatedRecurringPattern(recurringAppointmentRequest);
            appointments = new ArrayList<>(appointmentRecurringPattern.getAppointments());
        }
        return appointments;
    }
}
