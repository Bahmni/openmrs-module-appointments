package org.openmrs.module.appointments.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
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
    public List<AppointmentDefaultResponse> getAllAppointments(@RequestParam(value = "forDate", required = false) String forDate) throws ParseException {
        List<Appointment> appointments = appointmentsService.getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate));
        return appointmentMapper.constructResponse(appointments);
    }
    @RequestMapping( method = RequestMethod.POST, value = "search")
    @ResponseBody
    public List<AppointmentDefaultResponse> searchAppointments( @Valid @RequestBody AppointmentQuery searchQuery) throws IOException {
        Appointment appointment = appointmentMapper.mapQueryToAppointment(searchQuery);
        if (searchQuery.getStatus() == null) {
            appointment.setStatus(null);
        }
        List<Appointment> appointments =  appointmentsService.search(appointment);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> saveAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest){
        try {
            /**
             * The above code has been done so because to make appointment save within a transaction boundary.
             * calling appointmentMapper starts a transaction and persistent object is modified, which when
             * appointmentService.validateAndSave() is called was being written to DB, as calling validateAndSave() will
             * start another transaction, and before that the dirty persistent entity will be flushed to DB.
             * appointmentMapper should be fixed and validateAndSave() signature should be changed.
             */
            Appointment appointment = appointmentsService.validateAndSave(() -> appointmentMapper.fromRequest(appointmentRequest));
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping( method = RequestMethod.GET, value = "futureAppointmentsForServiceType")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllFututreAppointmentsForGivenServiceType(@RequestParam(value = "appointmentServiceTypeUuid", required = true) String serviceTypeUuid) {
        AppointmentServiceType appointmentServiceType = appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid(serviceTypeUuid);
        List<Appointment> appointments = appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.GET, value = "appointmentSummary")
    @ResponseBody
    public List<AppointmentsSummary> getAllAppointmentsSummary(@RequestParam(value = "startDate") String startDateString, @RequestParam(value = "endDate") String endDateString) throws ParseException {
        List<AppointmentsSummary> appointmentsSummaryList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = appointmentServiceDefinitionService.getAllAppointmentServices(false);
        for (AppointmentServiceDefinition appointmentServiceDefinition : appointmentServiceDefinitions) {
            List<Appointment> appointmentsForService =
                    appointmentsService.getAppointmentsForService(
                            appointmentServiceDefinition, startDate, endDate,
                            Arrays.asList(
                                    AppointmentStatus.Requested,
                                    AppointmentStatus.Completed,
                                    AppointmentStatus.Scheduled,
                                    AppointmentStatus.CheckedIn,
                                    AppointmentStatus.Missed));

            Map<Date, List<Appointment>> appointmentsGroupedByDate =
                    appointmentsForService.stream().collect(Collectors.groupingBy(Appointment::getDateFromStartDateTime));

            Map<String, DailyAppointmentServiceSummary> appointmentCountMap = new LinkedHashMap<>();
            for (Map.Entry<Date, List<Appointment>> appointmentDateMap : appointmentsGroupedByDate.entrySet()) {
                List<Appointment> appointments = appointmentDateMap.getValue();
                Long missedAppointmentsCount = appointments.stream().filter(s-> s.getStatus().equals(AppointmentStatus.Missed)).count();
                DailyAppointmentServiceSummary dailyAppointmentServiceSummary = new DailyAppointmentServiceSummary(
                        appointmentDateMap.getKey(), appointmentServiceDefinition.getUuid(), appointments.size(),Math.toIntExact(missedAppointmentsCount));
                appointmentCountMap.put(simpleDateFormat.format(appointmentDateMap.getKey()), dailyAppointmentServiceSummary);
            }

            AppointmentsSummary appointmentsSummary = new AppointmentsSummary(appointmentServiceMapper.constructDefaultResponse(appointmentServiceDefinition), appointmentCountMap);
            appointmentsSummaryList.add(appointmentsSummary);
        }
        return appointmentsSummaryList;
    }

    @RequestMapping(method = RequestMethod.POST, value="undoStatusChange/{appointmentUuid}")
    @ResponseBody
    public ResponseEntity<Object> undoStatusChange(@PathVariable("appointmentUuid")String appointmentUuid) throws ParseException {
        try{
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if(appointment == null){
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
    public AppointmentDefaultResponse getAppointmentByUuid(@RequestParam(value = "uuid") String uuid)  {
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        if(appointment == null) {
            log.error("Invalid. Appointment does not exist. UUID - " + uuid);
            throw new RuntimeException("Appointment does not exist");
        }
        return appointmentMapper.constructResponse(appointment);
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppointmentDefaultResponse getAppointmentById(@RequestParam(value = "id") Integer id) {
        Appointment appointment = appointmentsService.getAppointmentById(id);
        if(appointment == null) {
            log.error("Invalid. Related appointment does not exist. ID - " + id);
            throw new RuntimeException("Related appointment does not exist");
        }
        return appointmentMapper.constructResponse(appointment);
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
            appointmentsService.updateAppointmentProviderResponse(appointmentProviderProvider);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e) {
            log.error("Runtime error while trying to update appointment provider response", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    //TODO write test
    @RequestMapping(method = RequestMethod.POST, value="/{uuid}/reschedule")
    @ResponseBody
    public ResponseEntity<Object> rescheduleAppointment(@PathVariable("uuid") String prevAppointmentUuid,
                                                        @Valid @RequestBody AppointmentRequest appointmentRequest,
                                                        @RequestParam(value = "retainNumber", required = false, defaultValue = "false") boolean retainAppointmentNumber)
            throws ParseException {
        try {
            appointmentRequest.setUuid(null);
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            Appointment rescheduledAppointment = appointmentsService.reschedule(prevAppointmentUuid, appointment, false);
            return new ResponseEntity<>(appointmentMapper.constructResponse(rescheduledAppointment), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns a list of appointment on a date and status
     * @param forDate the appointment date
     * @param status - the appointment status i.e. scheduled, cancelled, completed,
     * @return list
     */
    @RequestMapping(method = RequestMethod.GET, value = "/appointmentStatus")
    @ResponseBody
    public ResponseEntity<Object> getAppointmentByDateAndStatus(@RequestParam(value = "forDate") String forDate, @RequestParam(value = "status") String status)  {
        try {
            if(StringUtils.isEmpty(forDate) || StringUtils.isEmpty(status)) {
                return new ResponseEntity<>("The request requires appointment date and status", HttpStatus.BAD_REQUEST);
            }
            List<Appointment> appointments = appointmentsService.getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate), status);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointments), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to fetch appointments by status and date", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "appointmentCalendar")
    @ResponseBody
    public List<AppointmentsSummary> getAllAppointmentsSummaryForAllServices(@RequestParam(value = "startDate") String startDateString, @RequestParam(value = "endDate") String endDateString) throws ParseException {
        List<AppointmentsSummary> appointmentsSummaryList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        List<Appointment> allAppointmentsInDateRange =
                appointmentsService.getAllAppointmentsInDateRange(startDate, endDate);

        String appointmentDate = new String();
        

        Map<Date, Map<AppointmentServiceDefinition, List<Appointment>>> groupAppointmentByDateAndService = allAppointmentsInDateRange.stream().collect (
                Collectors.groupingBy(Appointment::getDateFromStartDateTime,Collectors.groupingBy(Appointment::getService)));


        for (Map.Entry<Date, Map<AppointmentServiceDefinition, List<Appointment>>> appointmentDateMap : groupAppointmentByDateAndService.entrySet()) {
            List<DailyAppointmentServiceSummary> services = new ArrayList<>();
            appointmentDate = simpleDateFormat.format(appointmentDateMap.getKey());
            Integer totalServiceCount = 0;

            Map<AppointmentServiceDefinition, List<Appointment>> appointmentsBySevice = appointmentDateMap.getValue();


            for (Map.Entry<AppointmentServiceDefinition, List<Appointment>> appointmentDef : appointmentsBySevice.entrySet()) {
                List<Appointment> appointmentsByService = appointmentDef.getValue();
                DailyAppointmentServiceSummary dailyAppointmentServiceSummary = new DailyAppointmentServiceSummary(
                        null, appointmentDef.getKey().getUuid(), appointmentsByService.size(),
                        null, appointmentDef.getKey().getName());
                       
                services.add(dailyAppointmentServiceSummary);
                totalServiceCount += appointmentsByService.size();

            }
            AppointmentsSummary dailAappointmentsSummary = new AppointmentsSummary(services,appointmentDate,totalServiceCount);
            appointmentsSummaryList.add(dailAappointmentsSummary);
        }

        return appointmentsSummaryList;
    }

}
