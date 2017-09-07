package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.*;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
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
public class AppointmentController {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentServiceService appointmentServiceService;

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
        List<Appointment> appointments =  appointmentsService.search(appointment);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createAppointment(@Valid @RequestBody AppointmentPayload appointmentPayload) throws IOException {
        try {
            Appointment appointment = appointmentMapper.getAppointmentFromPayload(appointmentPayload);
            appointmentsService.save(appointment);
            return new ResponseEntity<>(appointment.getUuid(), HttpStatus.OK);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping( method = RequestMethod.GET)
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllFututreAppointmentsForGivenServiceType(@RequestParam(value = "appointmentServiceTypeUuid", required = true) String serviceTypeUuid) {
        AppointmentServiceType appointmentServiceType = appointmentServiceService.getAppointmentServiceTypeByUuid(serviceTypeUuid);
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
        List<AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentServices(false);
        for (AppointmentService appointmentService : appointmentServices) {
            List<Appointment> appointmentsForService =
                    appointmentsService.getAppointmentsForService(
                appointmentService, startDate, endDate,
                            Arrays.asList(
                                    AppointmentStatus.Completed,
                                    AppointmentStatus.Scheduled,
                                    AppointmentStatus.CheckedIn,
                                    AppointmentStatus.Missed));

            Map<Date, List<Appointment>> appointmentsGroupedByDate =
                    appointmentsForService.stream().collect(Collectors.groupingBy(Appointment::getDateFromStartDateTime));

            Map<String, AppointmentCount> appointmentCountMap = new LinkedHashMap<>();
            for (Map.Entry<Date, List<Appointment>> appointmentDateMap : appointmentsGroupedByDate.entrySet()) {
                List<Appointment> appointments = appointmentDateMap.getValue();
                Long missedAppointmentsCount = appointments.stream().filter(s-> s.getStatus().equals(AppointmentStatus.Missed)).count();
                AppointmentCount appointmentCount = new AppointmentCount(
                        appointments.size(),Math.toIntExact(missedAppointmentsCount), appointmentDateMap.getKey(), appointmentService.getUuid());
                appointmentCountMap.put(simpleDateFormat.format(appointmentDateMap.getKey()), appointmentCount);
            }

            AppointmentsSummary appointmentsSummary = new AppointmentsSummary(appointmentServiceMapper.constructDefaultResponse(appointmentService), appointmentCountMap);
            appointmentsSummaryList.add(appointmentsSummary);
        }
        return appointmentsSummaryList;
    }

    @RequestMapping(method = RequestMethod.POST, value="/{appointmentUuid}/changeStatus")
    @ResponseBody
    public ResponseEntity<Object> transitionAppointment(@PathVariable("appointmentUuid")String appointmentUuid, @RequestBody Map<String, String> statusDetails) throws ParseException {
        try {
            String toStatus = statusDetails.get("toStatus");
            Date onDate = DateUtil.convertToLocalDateFromUTC(statusDetails.get("onDate"));
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if(appointment != null){
                appointmentsService.changeStatus(appointment, toStatus, onDate);
                return new ResponseEntity<>(appointmentUuid, HttpStatus.OK);
            }else
                throw new RuntimeException("Appointment does not exist");
        }catch (RuntimeException e) {
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
