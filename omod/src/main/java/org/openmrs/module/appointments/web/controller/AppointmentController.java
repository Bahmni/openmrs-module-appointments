package org.openmrs.module.appointments.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.util.PatientUtil;
import org.openmrs.module.appointments.web.contract.*;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointment")
public class AppointmentController extends BaseRestController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllAppointments(
            @RequestParam(value = "forDate", required = false) String forDate) throws ParseException {
        List<Appointment> appointments = appointmentsService
                .getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate));
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST, value = "search")
    @ResponseBody
    public List<AppointmentDefaultResponse> searchAppointments(@Valid @RequestBody AppointmentQuery searchQuery)
            throws IOException {
        Appointment appointment = appointmentMapper.mapQueryToAppointment(searchQuery);
        if (searchQuery.getStatus() == null) {
            appointment.setStatus(null);
        }
        List<Appointment> appointments = appointmentsService.search(appointment);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> saveAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest) {
        try {
            /**
             * The above code has been done so because to make appointment save within a
             * transaction boundary.
             * calling appointmentMapper starts a transaction and persistent object is
             * modified, which when
             * appointmentService.validateAndSave() is called was being written to DB, as
             * calling validateAndSave() will
             * start another transaction, and before that the dirty persistent entity will
             * be flushed to DB.
             * appointmentMapper should be fixed and validateAndSave() signature should be
             * changed.
             */
            Appointment appointment = appointmentsService
                    .validateAndSave(() -> appointmentMapper.fromRequest(appointmentRequest));
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "futureAppointmentsForServiceType")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllFututreAppointmentsForGivenServiceType(
            @RequestParam(value = "appointmentServiceTypeUuid", required = true) String serviceTypeUuid) {
        AppointmentServiceType appointmentServiceType = appointmentServiceDefinitionService
                .getAppointmentServiceTypeByUuid(serviceTypeUuid);
        List<Appointment> appointments = appointmentsService
                .getAllFutureAppointmentsForServiceType(appointmentServiceType);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.GET, value = "appointmentSummary")
    @ResponseBody
    public List<AppointmentsSummary> getAllAppointmentsSummary(
            @RequestParam(value = "startDate") String startDateString,
            @RequestParam(value = "endDate") String endDateString) throws ParseException {
        List<AppointmentsSummary> appointmentsSummaryList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = appointmentServiceDefinitionService
                .getAllAppointmentServices(false);
        for (AppointmentServiceDefinition appointmentServiceDefinition : appointmentServiceDefinitions) {
            List<Appointment> appointmentsForService = appointmentsService.getAppointmentsForService(
                    appointmentServiceDefinition, startDate, endDate,
                    Arrays.asList(
                            AppointmentStatus.Requested,
                            AppointmentStatus.Completed,
                            AppointmentStatus.Scheduled,
                            AppointmentStatus.CheckedIn,
                            AppointmentStatus.Missed));

            Map<Date, List<Appointment>> appointmentsGroupedByDate = appointmentsForService.stream()
                    .collect(Collectors.groupingBy(Appointment::getDateFromStartDateTime));

            Map<String, DailyAppointmentServiceSummary> appointmentCountMap = new LinkedHashMap<>();
            for (Map.Entry<Date, List<Appointment>> appointmentDateMap : appointmentsGroupedByDate.entrySet()) {
                List<Appointment> appointments = appointmentDateMap.getValue();
                Long missedAppointmentsCount = appointments.stream()
                        .filter(s -> s.getStatus().equals(AppointmentStatus.Missed)).count();
                DailyAppointmentServiceSummary dailyAppointmentServiceSummary = new DailyAppointmentServiceSummary(
                        appointmentDateMap.getKey(), appointmentServiceDefinition.getUuid(), appointments.size(),
                        Math.toIntExact(missedAppointmentsCount));
                appointmentCountMap.put(simpleDateFormat.format(appointmentDateMap.getKey()),
                        dailyAppointmentServiceSummary);
            }

            AppointmentsSummary appointmentsSummary = new AppointmentsSummary(
                    appointmentServiceMapper.constructDefaultResponse(appointmentServiceDefinition),
                    appointmentCountMap);
            appointmentsSummaryList.add(appointmentsSummary);
        }
        return appointmentsSummaryList;
    }

    @RequestMapping(method = RequestMethod.POST, value = "undoStatusChange/{appointmentUuid}")
    @ResponseBody
    public ResponseEntity<Object> undoStatusChange(@PathVariable("appointmentUuid") String appointmentUuid)
            throws ParseException {
        try {
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if (appointment == null) {
                throw new RuntimeException("Appointment does not exist");
            }
            appointmentsService.undoStatusChange(appointment);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to undo appointment status", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppointmentDefaultResponse getAppointmentByUuid(@RequestParam(value = "uuid") String uuid) {
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        if (appointment == null) {
            log.error("Invalid. Appointment does not exist. UUID - " + uuid);
            throw new RuntimeException("Appointment does not exist");
        }
        return appointmentMapper.constructResponse(appointment);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{appointmentUuid}/providerResponse")
    @ResponseBody
    public ResponseEntity<Object> updateAppointmentProviderResponse(
            @PathVariable("appointmentUuid") String appointmentUuid,
            @RequestBody AppointmentProviderDetail providerResponse) throws ParseException {
        try {
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if (appointment == null) {
                throw new RuntimeException("Appointment does not exist");
            }
            AppointmentProvider appointmentProviderProvider = appointmentMapper
                    .mapAppointmentProvider(providerResponse);
            appointmentsService.updateAppointmentProviderResponse(appointmentProviderProvider);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to update appointment provider response", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // TODO write test
    @RequestMapping(method = RequestMethod.POST, value = "/{uuid}/reschedule")
    @ResponseBody
    public ResponseEntity<Object> rescheduleAppointment(@PathVariable("uuid") String prevAppointmentUuid,
            @Valid @RequestBody AppointmentRequest appointmentRequest,
            @RequestParam(value = "retainNumber", required = false, defaultValue = "false") boolean retainAppointmentNumber)
            throws ParseException {
        try {
            appointmentRequest.setUuid(null);
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            Appointment rescheduledAppointment = appointmentsService.reschedule(prevAppointmentUuid, appointment,
                    false);
            return new ResponseEntity<>(appointmentMapper.constructResponse(rescheduledAppointment), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns a list of appointment on a date and status
     * 
     * @param forDate the appointment date
     * @param status  - the appointment status i.e. scheduled, cancelled, completed,
     * @return list
     */
    @RequestMapping(method = RequestMethod.GET, value = "/appointmentStatus")
    @ResponseBody
    public ResponseEntity<Object> getAppointmentByDateAndStatus(@RequestParam(value = "forDate") String forDate,
            @RequestParam(value = "status") String status) {
        try {
            if (StringUtils.isEmpty(forDate) || StringUtils.isEmpty(status)) {
                return new ResponseEntity<>("The request requires appointment date and status", HttpStatus.BAD_REQUEST);
            }
            List<Appointment> appointments = appointmentsService
                    .getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate), status);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointments), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to fetch appointments by status and date", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "appointmentCalendar")
    @ResponseBody
    public List<AppointmentsSummary> getAllAppointmentsSummaryForAllServices(
            @RequestParam(value = "startDate") String startDateString,
            @RequestParam(value = "endDate") String endDateString) throws ParseException {
        List<AppointmentsSummary> appointmentsSummaryList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        List<Appointment> allAppointmentsInDateRange = appointmentsService.getAllAppointmentsInDateRange(startDate,
                endDate);

        String appointmentDate = new String();
        if (allAppointmentsInDateRange != null && allAppointmentsInDateRange.size() > 0) {
            Map<Date, Map<AppointmentServiceDefinition, List<Appointment>>> groupAppointmentByDateAndService = allAppointmentsInDateRange
            .stream().filter(appt -> appt.getDateFromStartDateTime()!= null && appt.getService() != null)
            .collect(Collectors.groupingBy(Appointment::getDateFromStartDateTime,Collectors.groupingBy(Appointment::getService)));

            for (Map.Entry<Date, Map<AppointmentServiceDefinition, List<Appointment>>> appointmentDateMap : groupAppointmentByDateAndService
                    .entrySet()) {
                List<DailyAppointmentServiceSummary> services = new ArrayList<>();
                appointmentDate = simpleDateFormat.format(appointmentDateMap.getKey());
                Integer totalServiceCount = 0;

                Map<AppointmentServiceDefinition, List<Appointment>> appointmentsBySevice = appointmentDateMap.getValue();

                for (Map.Entry<AppointmentServiceDefinition, List<Appointment>> appointmentDef : appointmentsBySevice
                        .entrySet()) {
                    List<Appointment> appointmentsByService = appointmentDef.getValue();
                    DailyAppointmentServiceSummary dailyAppointmentServiceSummary = new DailyAppointmentServiceSummary(
                            null, appointmentDef.getKey().getUuid(), appointmentsByService.size(),
                            null, appointmentDef.getKey().getName());

                    services.add(dailyAppointmentServiceSummary);
                    totalServiceCount += appointmentsByService.size();

                }
                AppointmentsSummary dailAappointmentsSummary = new AppointmentsSummary(services, appointmentDate,
                        totalServiceCount);
                appointmentsSummaryList.add(dailAappointmentsSummary);
            }


        }


        return appointmentsSummaryList;
    }

    /**
     * Returns a list of unscheduled appointment on a date
     * 
     * @param forDate the appointment date
     * @return list
     */
    @RequestMapping(method = RequestMethod.GET, value = "unScheduledAppointment")
    @ResponseBody
    public ResponseEntity<Object> getUnScheduledAppointments(@RequestParam(value = "forDate") String forDate) {
        
        try {
            if (StringUtils.isEmpty(forDate)) {
                return new ResponseEntity<>("The request requires appointment date", HttpStatus.BAD_REQUEST);
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date maxDate = new Date(DateUtil.convertToLocalDateFromUTC(forDate).getTime() + TimeUnit.DAYS.toMillis(1));
            Date minDate = formatter.parse(forDate);
            List<Integer> visitPatientIds = new ArrayList<>();
            List<Integer> appointmentPatientIds = new ArrayList<>();
            List<Appointment> appointments = appointmentsService
                    .getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate));
            List<Visit> visits = Context.getVisitService().getVisits(null, null, null, null, minDate, maxDate, null,
                    null, null, false, false);
            PatientService patientService = Context.getPatientService();
            List<Object> unScheduledPatients = new ArrayList<>();
            appointments.stream().forEach(p -> {
                appointmentPatientIds.add(p.getPatient().getPatientId());
            });

            visits.stream().forEach(v -> {
                visitPatientIds.add(v.getPatient().getPatientId());
            });

            List<Integer> unScheduledAppPatientIds = visitPatientIds.stream()
                    .filter(v -> !appointmentPatientIds.contains(v))
                    .collect(Collectors.toList());
            unScheduledAppPatientIds.stream().distinct().forEach(patientId -> {
                Map visitDetails = new HashMap();
                Map unscheduledPatientMap = new HashMap();
                if (!visits.isEmpty()) {
                    visits.forEach(visit -> {
                        if(visit.getPatient().getPatientId() == patientId) {
                            visitDetails.put("startDateTime",visit.getStartDatetime());
                            visitDetails.put("visitType", visit.getVisitType().getName());
                            visitDetails.put("stopDateTime", visit.getStopDatetime());

                        }
                    });
                }                
                unscheduledPatientMap.put("visit", visitDetails);
                unscheduledPatientMap.putAll(PatientUtil.patientMap(patientService.getPatient(patientId)));
                unScheduledPatients.add(unscheduledPatientMap);

            });

            return new ResponseEntity<>(unScheduledPatients, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to fetch appointments by date", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


     /**
     * Returns a list of of those who came early for their appointment on a particular date
     * 
     * @param forDate the appointment date
     * @return list
     */
    @RequestMapping(method = RequestMethod.GET, value = "earlyAppointment")
    @ResponseBody
    public ResponseEntity<Object> getAllEarlyAppointmentsByDate(@RequestParam(value = "forDate") String forDate) {
        try {
            if (StringUtils.isEmpty(forDate)) {
                return new ResponseEntity<>("The request requires appointment date", HttpStatus.BAD_REQUEST);
            }
            Date appointmentDate = DateUtil.convertToLocalDateFromUTC(forDate);
            List<Appointment> appointments = appointmentsService
                    .getAllCameEarlyAppointments(appointmentDate);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointments), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to fetch appointments by date", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a list of those who honored their appointments and have been checked out
     *
     * @param forDate the appointment date
     * @return list
     */
    @RequestMapping(method = RequestMethod.GET, value = "completedAppointment")
    @ResponseBody
    public ResponseEntity<Object> getAllCompletedAppointments(@RequestParam(value = "forDate") String forDate) {
        try {
            if (StringUtils.isEmpty(forDate)) {
                return new ResponseEntity<>("The request requires appointment date", HttpStatus.BAD_REQUEST);
            }
            Date appointmentDate = DateUtil.convertToLocalDateFromUTC(forDate);
            List<Appointment> appointments = appointmentsService
                    .getCompletedAppointments(appointmentDate);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointments), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to fetch completed appointments", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

 /**
     * Returns a list of all appointments in a given date range
     * 
     * @param startDate the start date
     *  @param endDate the end date
     * @return list
     */
    @RequestMapping(method = RequestMethod.GET, value = "appointmentsInDateRange")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllAppointmentsByDateRange(
        @RequestParam(value = "startDate") String startDateString,
        @RequestParam(value = "endDate") String endDateString) throws ParseException {
            Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
            Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
            List<AppointmentDefaultResponse> appointmentsDefaultResponse = new ArrayList<>();
            List<Appointment> allAppointmentsInDateRange = appointmentsService.getAllAppointmentsInDateRange(startDate,
                    endDate);
            if (allAppointmentsInDateRange != null && allAppointmentsInDateRange.size() > 0) {
                appointmentsDefaultResponse = appointmentMapper.constructResponse(allAppointmentsInDateRange);
            }
        
        return appointmentsDefaultResponse;
    }
}
