package org.openmrs.module.appointments.web.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentCount;
import org.openmrs.module.appointments.web.contract.AppointmentsSummary;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AppointmentSummaryService {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentServiceService appointmentServiceService;

    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    private List<AppointmentStatus> APPOINTMENT_STATUS_LIST = Arrays.asList(
            AppointmentStatus.Completed,
            AppointmentStatus.Scheduled,
            AppointmentStatus.CheckedIn,
            AppointmentStatus.Missed);

    public List<AppointmentsSummary> getSummaries(SimpleDateFormat simpleDateFormat, Date startDate, Date endDate) {
        List<AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentServices(false);
        return mapAppointmentServices(simpleDateFormat, startDate, endDate, appointmentServices);
    }

    private List<AppointmentsSummary> mapAppointmentServices(SimpleDateFormat simpleDateFormat, Date startDate, Date endDate, List<AppointmentService> appointmentServices) {
        return appointmentServices.stream().map(appointmentService -> {
            Map<String, AppointmentCount> appointmentCountMap = getAppointmentsCountMap(simpleDateFormat, startDate, endDate, appointmentService);

            return new AppointmentsSummary(appointmentServiceMapper.constructDefaultResponse(appointmentService), appointmentCountMap);
        }).collect(Collectors.toList());

    }

    private Map<String, AppointmentCount> getAppointmentsCountMap(SimpleDateFormat simpleDateFormat, Date startDate, Date endDate, AppointmentService appointmentService) {
        List<Appointment> appointmentsForService = appointmentsService.getAppointmentsForService(appointmentService, startDate, endDate, APPOINTMENT_STATUS_LIST);

        Map<Date, List<Appointment>> appointmentsGroupedByDate = appointmentsForService.stream().collect(Collectors.groupingBy(Appointment::getDateFromStartDateTime));

        return getAppointmentCountMap(simpleDateFormat, appointmentService, appointmentsGroupedByDate);
    }

    private Map<String, AppointmentCount> getAppointmentCountMap(SimpleDateFormat simpleDateFormat, AppointmentService appointmentService, Map<Date, List<Appointment>> appointmentsGroupedByDate) {
        Map<String, AppointmentCount> appointmentCountMap = new LinkedHashMap<>();
        appointmentsGroupedByDate.forEach((key, appointments) -> {
            AppointmentCount appointmentCount = new AppointmentCount(appointments.size(), Math.toIntExact(getMissedAppointmentsCount(appointments)), key, appointmentService.getUuid());
            appointmentCountMap.put(simpleDateFormat.format(key), appointmentCount);
        });

        return appointmentCountMap;
    }

    private long getMissedAppointmentsCount(List<Appointment> appointments) {
        return appointments.stream().filter(s -> s.getStatus().equals(AppointmentStatus.Missed)).count();
    }
}
