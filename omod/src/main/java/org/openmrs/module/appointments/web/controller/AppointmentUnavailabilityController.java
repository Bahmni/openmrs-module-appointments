package org.openmrs.module.appointments.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentUnavailabilityMapper;
import org.openmrs.module.appointments.web.validators.AppointmentUnavailabilityRequestValidator;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentUnavailability")
public class AppointmentUnavailabilityController extends BaseRestController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private AppointmentUnavailabilityService appointmentUnavailabilityService;

    @Autowired
    private AppointmentUnavailabilityMapper appointmentUnavailabilityMapper;

    @Autowired
    private AppointmentUnavailabilityRequestValidator unavailabilityRequestValidator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createAppointmentUnavailability(
            @RequestBody List<AppointmentUnavailabilityRequest> requests) {

        Errors errors = new BeanPropertyBindingResult(requests, "appointmentUnavailabilityRequests");
        unavailabilityRequestValidator.validate(requests, errors);
        if (!errors.getAllErrors().isEmpty()) {
            log.error("Validation failed: " + errors.getAllErrors().get(0).getDefaultMessage());
            throw new APIException(errors.getAllErrors().get(0).getDefaultMessage());
        }

        List<AppointmentUnavailability> unavailabilities = appointmentUnavailabilityMapper.fromRequest(requests);
        List<AppointmentUnavailability> savedUnavailabilities = appointmentUnavailabilityService.save(unavailabilities);
        List<AppointmentUnavailabilityResponse> responses = appointmentUnavailabilityMapper.constructResponse(savedUnavailabilities);

        log.info("Successfully created " + savedUnavailabilities.size() + " appointment unavailability block(s)");
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getAllAppointmentUnavailabilities(
            @RequestParam(value = "locationUuid", required = false) String locationUuid,
            @RequestParam(value = "serviceUuid", required = false) String serviceUuid,
            @RequestParam(value = "providerUuid", required = false) String providerUuid,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "includeVoided", required = false, defaultValue = "false") boolean includeVoided,
            @RequestParam(value = "limit", required = false) Integer limit) throws ParseException {

        log.info("Fetching appointment unavailabilities with filters - location: " + locationUuid +
                ", service: " + serviceUuid + ", provider: " + providerUuid);

        Date startDate = null;
        if (StringUtils.isNotBlank(startDateStr)) {
            startDate = java.sql.Date.valueOf(LocalDate.parse(startDateStr, DATE_FORMATTER));
        }

        Date endDate = null;
        if (StringUtils.isNotBlank(endDateStr)) {
            endDate = java.sql.Date.valueOf(LocalDate.parse(endDateStr, DATE_FORMATTER));
        }

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(locationUuid);
        searchParams.setServiceUuid(serviceUuid);
        searchParams.setProviderUuid(providerUuid);
        searchParams.setStartDate(startDate);
        searchParams.setEndDate(endDate);
        searchParams.setIncludeVoided(includeVoided);
        searchParams.setLimit(limit);

        List<AppointmentUnavailability> unavailabilities = appointmentUnavailabilityService.getAll(searchParams);
        List<AppointmentUnavailabilityResponse> responses = appointmentUnavailabilityMapper.constructResponse(unavailabilities);

        log.info("Retrieved " + unavailabilities.size() + " appointment unavailability block(s)");
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
    @ResponseBody
    public ResponseEntity<Object> getAppointmentUnavailabilityByUuid(@PathVariable("uuid") String uuid) {
        AppointmentUnavailability unavailability = appointmentUnavailabilityService.getByUuid(uuid);
        if (unavailability == null) {
            log.error("Could not identify appointment unavailability with uuid: " + uuid);
            return new ResponseEntity<>("Appointment Unavailability not found", HttpStatus.NOT_FOUND);
        }
        AppointmentUnavailabilityResponse response = appointmentUnavailabilityMapper.constructResponse(unavailability);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{uuid}")
    @ResponseBody
    public ResponseEntity<Object> voidAppointmentUnavailability(
            @PathVariable("uuid") String uuid,
            @RequestParam(value = "voidReason", required = false) String voidReason) {
        AppointmentUnavailability unavailability = appointmentUnavailabilityService.getByUuid(uuid);
        if (unavailability == null) {
            log.error("Could not identify appointment unavailability with uuid: " + uuid);
            return new ResponseEntity<>("Appointment Unavailability not found", HttpStatus.NOT_FOUND);
        }
        if (!unavailability.getVoided()) {
            appointmentUnavailabilityService.voidAppointmentUnavailability(unavailability, voidReason);
        }

        log.info("Successfully voided appointment unavailability: " + uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler({RuntimeException.class, APIException.class, ParseException.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("Exception in AppointmentUnavailabilityController", ex);
        Map<String, Object> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
