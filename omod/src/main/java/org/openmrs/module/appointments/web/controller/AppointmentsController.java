package org.openmrs.module.appointments.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.validators.AppointmentSearchValidator;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
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
public class AppointmentsController extends BaseRestController {
    @Autowired
    private AppointmentsService appointmentsService;
    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentSearchValidator appointmentSearchValidator;

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
    public List<AppointmentDefaultResponse> search(@Valid @RequestBody AppointmentSearchRequest appointmentSearchRequest) {
        Errors appointmentSearchErrors = new BeanPropertyBindingResult(appointmentSearchRequest, "appointmentSearchRequest");
        appointmentSearchValidator.validate(appointmentSearchRequest, appointmentSearchErrors);
        if (!appointmentSearchErrors.getAllErrors().isEmpty()) {
            throw new RuntimeException(appointmentSearchErrors.getAllErrors().get(0).getDefaultMessage());
        }
        List<Appointment> appointments = appointmentsService.search(appointmentSearchRequest);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{appointmentUuid}/status-change")
    @ResponseBody
    public ResponseEntity<Object> transitionAppointment(@PathVariable("appointmentUuid") String appointmentUuid, @RequestBody Map<String, String> statusDetails) throws ParseException {
        String toStatus = statusDetails.get("toStatus");
        Date onDate = DateUtil.convertToLocalDateFromUTC(statusDetails.get("onDate"));
        Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
        if (appointment != null) {
            appointmentsService.changeStatus(appointment, toStatus, onDate);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
        } else
            throw new RuntimeException("Appointment does not exist");
    }

    @RequestMapping(method = RequestMethod.POST, value = "/conflicts")
    @ResponseBody
    public ResponseEntity<Object> getConflicts(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            Appointment appointment = appointmentMapper.fromRequestClonedAppointment(appointmentRequest);
            Map<Enum, List<Appointment>> appointmentConflicts = appointmentsService.getAppointmentConflicts(appointment);
            if (appointmentConflicts.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(appointmentMapper.constructConflictResponse(appointmentConflicts), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to get getConflicts for appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value="/{appointmentUuid}/providerResponse")
    @ResponseBody
    public ResponseEntity<Object> updateAppointmentProviderResponse(@PathVariable("appointmentUuid")String appointmentUuid, @RequestBody AppointmentProviderDetail providerResponse) throws ParseException {
        try {
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if(appointment == null){
                throw new RuntimeException("Appointment does not exist");
            }
            AppointmentProvider appointmentProviderProvider = appointmentMapper.mapAppointmentProvider(providerResponse);
            appointmentProviderProvider.setAppointment(appointment);
            appointmentsService.updateAppointmentProviderResponse(appointmentProviderProvider);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e) {
            log.error("Runtime error while trying to update appointment provider response", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
