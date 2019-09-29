package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AppointmentDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentDao appointmentDao;

    @Autowired
    AppointmentServiceDao appointmentServiceDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void shouldGetAllNonVoidedAppointments() throws Exception {
        List<Appointment> allAppointmentServices = appointmentDao.getAllAppointments(null);
        assertEquals(11, allAppointmentServices.size());
    }

    @Test
    public void shouldGetAllNonVoidedAppointmentsForDate() throws Exception {
        Date forDate = DateUtil.convertToDate("2108-08-15T00:00:00.0Z", DateUtil.DateFormatType.UTC);
        List<Appointment> allAppointments = appointmentDao.getAllAppointments(forDate);
        assertEquals(3, allAppointments.size());
    }

    @Test
    public void shouldSaveAppointmentService() throws Exception {
        List<Appointment> allAppointments = appointmentDao.getAllAppointments(null);
        assertEquals(11, allAppointments.size());
        Appointment apt = new Appointment();
        apt.setPatient(allAppointments.get(0).getPatient());
        appointmentDao.save(apt);
        allAppointments = appointmentDao.getAllAppointments(null);
        assertEquals(12, allAppointments.size());
    }

    @Test
    public void shouldGetAllFutureAppointmentForTheGivenService() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        List<Appointment> allAppointments = appointmentDao.getAllFutureAppointmentsForService(appointmentServiceDefinition);
        assertNotNull(allAppointments);
        assertEquals(2, allAppointments.size());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c1111", allAppointments.get(0).getUuid());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c12222", allAppointments.get(1).getUuid());
    }

    @Test
    public void shouldGetAllNonVoidedAndNonCancelledFutureAppointmentFortheGivenServiceType() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        Set<AppointmentServiceType> serviceTypes = appointmentServiceDefinition.getServiceTypes();
        AppointmentServiceType appointmentServiceType = serviceTypes.iterator().next();
        List<Appointment> allFutureAppointmentsForServiceType = appointmentDao.getAllFutureAppointmentsForServiceType(appointmentServiceType);
        assertNotNull(allFutureAppointmentsForServiceType);
        assertEquals(1,allFutureAppointmentsForServiceType.size());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c13346", allFutureAppointmentsForServiceType.get(0).getUuid());
    }

    @Test
    public void shouldGetAppointmentServicesInADateRange() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse("2017-08-08");
        Date endDate = simpleDateFormat.parse("2017-08-09");
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        List<Appointment> appointmentsForService = appointmentDao.getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, null);
        Iterator iterator = appointmentsForService.iterator();
        Appointment appointmentWithNonVoidedServiceType  = (Appointment) iterator.next();
        Appointment appointmentWithoutServiceType  = (Appointment) iterator.next();
        assertEquals(2, appointmentsForService.size());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c13349", appointmentWithNonVoidedServiceType.getUuid());
        assertEquals(false, appointmentWithNonVoidedServiceType.getVoided());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c13351", appointmentWithoutServiceType.getUuid());
        assertEquals(false, appointmentWithoutServiceType.getVoided());
    }

    @Test
    public void shouldFilterAppointmentsByStatus() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse("2017-08-08");
        Date endDate = simpleDateFormat.parse("2017-08-09");
        List<AppointmentStatus> appointmentStatusList = new ArrayList<>();
        appointmentStatusList.add(AppointmentStatus.Scheduled);
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        List<Appointment> appointmentsForService = appointmentDao.getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, appointmentStatusList);
        assertEquals(1, appointmentsForService.size());
        Iterator iterator = appointmentsForService.iterator();
        Appointment appointment1  = (Appointment) iterator.next();
        assertEquals(1, appointmentsForService.size());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c13349", appointment1.getUuid());
        assertEquals(AppointmentStatus.Scheduled, appointment1.getStatus());
    }

    @Test
    public void shouldGetAppointmentByUuid() throws Exception {
        String appointmentUuid="75504r42-3ca8-11e3-bf2b-0800271c1b77";
        Appointment appointment = appointmentDao.getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals(appointmentUuid, appointment.getUuid());
    }

    @Test
    public void shouldGetReturnNullWhenAppointmentDoesNotExist() throws Exception {
        String appointmentUuid="75504r42-3ca8-11e3-bf2b-0800271c1b78";
        Appointment appointment = appointmentDao.getAppointmentByUuid(appointmentUuid);
        assertNull(appointment);
    }

    @Test
    public void shouldGetAppointmentsForGivenDateRange() throws Exception {
        Date from = DateUtil.convertToDate("2108-08-10T00:00:00.0Z", DateUtil.DateFormatType.UTC);
        Date to = DateUtil.convertToDate("2108-08-15T00:00:00.0Z", DateUtil.DateFormatType.UTC);
        List<Appointment> allAppointments = appointmentDao.getAllAppointmentsInDateRange(from, to);
        assertEquals(1, allAppointments.size());
    }

    @Test
    public void shouldGetAppointmentsBeforeCurrentDateWhenStartDateIsNotProvided() throws ParseException {
        Date to = DateUtil.convertToDate("2108-08-15T00:00:00.0Z", DateUtil.DateFormatType.UTC);
        List<Appointment> allAppointments = appointmentDao.getAllAppointmentsInDateRange(null, to);
        assertEquals(6, allAppointments.size());
    }

    @Test
    public void shouldGetAllNonVoidedAppointmentsWhenNoDateRangeIsProvided() throws Exception {
        List<Appointment> allAppointmentServices = appointmentDao.getAllAppointmentsInDateRange(null, null);
        assertEquals(11, allAppointmentServices.size());
    }

    @Test
    public void shouldSearchAppointmentsForAPatient() {
        List<Appointment> allAppointments = appointmentDao.getAllAppointments(null);
        Appointment appointment = new Appointment();
        appointment.setPatient(allAppointments.get(0).getPatient());
        appointment.setLocation(allAppointments.get(0).getLocation());
        appointment.setService(allAppointments.get(0).getService());
        appointment.setStatus(null);
        List<Appointment> searchedAppointmentList = appointmentDao.search(appointment);
        assertEquals(1, searchedAppointmentList.size());
    }

    @Test
    public void shouldReturnAllAppointmentsBetweenGivenDates() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2108-08-13T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDate = DateUtil.convertToDate("2108-08-16T18:29:59.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(5, appointments.size());
    }

    @Test
    public void shouldReturnAllAppointmentsInNoGivenDates() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(11, appointments.size());
    }

    @Test
    public void shouldReturnAllNonVoidedFutureAppointmentsOfPatient() {
        List<Appointment> appointments = appointmentDao.getAppointmentsForPatient(1);
        assertEquals(5, appointments.size());
    }
}
