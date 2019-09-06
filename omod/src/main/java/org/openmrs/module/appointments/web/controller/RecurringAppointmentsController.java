package org.openmrs.module.appointments.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.helper.RecurringPatternHelper;
import org.openmrs.module.appointments.web.mapper.AbstractAppointmentRecurringPatternMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringAppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.validators.Validator;
import org.openmrs.module.appointments.web.validators.impl.RecurringPatternValidator;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/recurring-appointments")
public class RecurringAppointmentsController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Autowired
    private RecurringPatternValidator recurringPatternValidator;

    @Autowired
    private RecurringPatternHelper recurringPatternHelper;

    @Autowired
    private RecurringAppointmentMapper recurringAppointmentMapper;

    @Autowired
    private RecurringPatternMapper recurringPatternMapper;

    @Autowired
    @Qualifier("appointmentRequestEditValidator")
    Validator<RecurringAppointmentRequest> appointmentRequestEditValidator;

    @Autowired
    @Qualifier("singleAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper singleAppointmentRecurringPatternMapper;

    @Autowired
    @Qualifier("allAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper allAppointmentRecurringPatternMapper;

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
            AppointmentRecurringPattern appointmentRecurringPattern = recurringPatternMapper.fromRequest(recurringPattern);
            List<Appointment> appointmentsList = recurringPatternHelper.generateRecurringAppointments(appointmentRecurringPattern,
                    recurringAppointmentRequest);
            appointmentRecurringPattern.setAppointments(new HashSet<>(appointmentsList));
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
            final boolean isValidAppointmentRequest = appointmentRequestEditValidator.validate(recurringAppointmentRequest);
            if (!isValidAppointmentRequest)
                throw new APIException(appointmentRequestEditValidator.getError());
            boolean applyForAll = recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments();
            final String appointmentRequestUuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
            if (applyForAll) {
                AppointmentRecurringPattern recurringPattern = allAppointmentRecurringPatternMapper.fromRequest(recurringAppointmentRequest);
                AppointmentRecurringPattern updatedAppointmentRecurringPattern = appointmentRecurringPatternService.update(recurringPattern);
                List<Appointment> updatedAppointments = new ArrayList<>(updatedAppointmentRecurringPattern.getAppointments());
                return new ResponseEntity<>(recurringAppointmentMapper.constructResponse(updatedAppointments), HttpStatus.OK);
            } else {
                    AppointmentRecurringPattern appointmentRecurringPattern = singleAppointmentRecurringPatternMapper.fromRequest(recurringAppointmentRequest);
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
}
