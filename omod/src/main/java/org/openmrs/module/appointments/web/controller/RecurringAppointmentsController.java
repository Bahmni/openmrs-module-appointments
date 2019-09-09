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
import org.openmrs.module.appointments.web.mapper.AppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.mapper.RecurringAppointmentMapper;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.openmrs.module.appointments.web.validators.RecurringPatternValidator;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    @Qualifier("singleAppointmentRecurringPatternUpdateService")
    private AppointmentRecurringPatternUpdateService singleAppointmentRecurringPatternUpdateService;

    @Autowired
    @Qualifier("allAppointmentRecurringPatternUpdateService")
    private AppointmentRecurringPatternUpdateService allAppointmentRecurringPatternUpdateService;

    @Autowired
    private AppointmentsService appointmentsService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody RecurringAppointmentRequest recurringAppointmentRequest) {
        try {
            RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
            Errors errors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");
            recurringPatternValidator.validate(recurringPattern, errors);
            if (!errors.getAllErrors().isEmpty()) {
                throw new APIException(errors.getAllErrors().get(0).getCodes()[1]);
            }
            AppointmentRecurringPattern appointmentRecurringPattern =
                    recurringAppointmentsService.generateAppointmentRecurringPatternWithAppointments(recurringAppointmentRequest);
            appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern);
            return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(
                    new ArrayList<>(appointmentRecurringPattern.getAppointments())), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to create recurring appointments", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{appointmentUuid}")
    @ResponseBody
    public ResponseEntity<Object> editAppointment(@Valid @RequestBody RecurringAppointmentRequest recurringAppointmentRequest) {
        try {
            RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
            Errors errors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");
            recurringPatternValidator.validate(recurringPattern, errors);
            if (!errors.getAllErrors().isEmpty()) {
                throw new APIException(errors.getAllErrors().get(0).getCodes()[1]);
            }
            boolean applyForAll = recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments();
            final String appointmentRequestUuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
            if (applyForAll) {
                AppointmentRecurringPattern appointmentRecurringPattern = allAppointmentRecurringPatternUpdateService.fromRequest(recurringAppointmentRequest);
                AppointmentRecurringPattern updatedAppointmentRecurringPattern = appointmentRecurringPatternService.update(appointmentRecurringPattern);
                List<Appointment> updatedAppointments = new ArrayList<>(updatedAppointmentRecurringPattern.getAppointments());
                return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(updatedAppointments), HttpStatus.OK);
            } else {
                    AppointmentRecurringPattern appointmentRecurringPattern = singleAppointmentRecurringPatternUpdateService.fromRequest(recurringAppointmentRequest);
                    AppointmentRecurringPattern updatedAppointmentRecurringPattern = appointmentRecurringPatternService.update(appointmentRecurringPattern);
                    Appointment updatedAppointment = null;
                    final Set<Appointment> updatedAppointments = updatedAppointmentRecurringPattern.getActiveAppointments();
                    final Iterator<Appointment> iterator = updatedAppointments.iterator();
                    while (iterator.hasNext()) {
                        final Appointment currentAppointment = iterator.next();
                        final Appointment relatedAppointment = currentAppointment.getRelatedAppointment();
                        if ((currentAppointment.getUuid().equals(appointmentRequestUuid) && !currentAppointment.getVoided())
                                || (relatedAppointment != null && relatedAppointment.getUuid().equals(appointmentRequestUuid))) {
                            updatedAppointment = currentAppointment;
                            break;
                        }
                    }
                    return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(updatedAppointment), HttpStatus.OK);
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
}
