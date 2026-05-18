package org.openmrs.module.appointments.web.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentUnavailability")
public class AppointmentUnavailabilityController extends BaseRestController {

    @Autowired
    private AppointmentUnavailabilityService appointmentUnavailabilityService;

    @Autowired
    private AppointmentUnavailabilityMapper appointmentUnavailabilityMapper;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    private AppointmentUnavailabilityRequestValidator unavailabilityRequestValidator;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createAppointmentUnavailability(
            @RequestBody List<AppointmentUnavailabilityRequest> requests) {

        Errors errors = new BeanPropertyBindingResult(requests, "appointmentUnavailabilityRequests");
        unavailabilityRequestValidator.validate(requests, errors);
        if (!errors.getAllErrors().isEmpty()) {
            throw new RuntimeException(errors.getAllErrors().get(0).getDefaultMessage());
        }

        List<AppointmentUnavailability> unavailabilities = appointmentUnavailabilityMapper.fromRequest(requests);
        List<AppointmentUnavailability> savedUnavailabilities = appointmentUnavailabilityService.save(unavailabilities);
        List<AppointmentUnavailabilityResponse> responses = appointmentUnavailabilityMapper.constructResponse(savedUnavailabilities);
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

        Location location = null;
        if (StringUtils.isNotBlank(locationUuid)) {
            location = locationService.getLocationByUuid(locationUuid);
        }

        AppointmentServiceDefinition service = null;
        if (StringUtils.isNotBlank(serviceUuid)) {
            service = appointmentServiceDefinitionService.getAppointmentServiceByUuid(serviceUuid);
        }

        Provider provider = null;
        if (StringUtils.isNotBlank(providerUuid)) {
            provider = providerService.getProviderByUuid(providerUuid);
        }

        Date startDate = null;
        if (StringUtils.isNotBlank(startDateStr)) {
            startDate = DATE_FORMAT.parse(startDateStr);
        }

        Date endDate = null;
        if (StringUtils.isNotBlank(endDateStr)) {
            endDate = DATE_FORMAT.parse(endDateStr);
        }

        List<AppointmentUnavailability> unavailabilities = appointmentUnavailabilityService.getAll(
                location, service, provider, startDate, endDate, includeVoided, limit);
        List<AppointmentUnavailabilityResponse> responses = appointmentUnavailabilityMapper.constructResponse(unavailabilities);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
    @ResponseBody
    public ResponseEntity<Object> getAppointmentUnavailabilityByUuid(@PathVariable("uuid") String uuid) {
        AppointmentUnavailability unavailability = appointmentUnavailabilityService.getByUuid(uuid);
        if (unavailability == null) {
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
            return new ResponseEntity<>("Appointment Unavailability not found", HttpStatus.NOT_FOUND);
        }
        if (unavailability.getVoided()) {
            AppointmentUnavailabilityResponse response = appointmentUnavailabilityMapper.constructResponse(unavailability);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        appointmentUnavailabilityService.voidAppointmentUnavailability(unavailability, voidReason);
        AppointmentUnavailability voidedUnavailability = appointmentUnavailabilityService.getByUuid(uuid);
        AppointmentUnavailabilityResponse response = appointmentUnavailabilityMapper.constructResponse(voidedUnavailability);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler({RuntimeException.class, ParseException.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
