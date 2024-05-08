package org.openmrs.module.appointments.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

public class AppointmentDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentDao appointmentDao;

    @Autowired
    AppointmentServiceDao appointmentServiceDao;

    @Autowired
    EncounterService encounterService;

    @Autowired
    SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void shouldGetAllNonVoidedAppointments() throws Exception {
        List<Appointment> allAppointmentServices = appointmentDao.getAllAppointments(null);
        assertEquals(13, allAppointmentServices.size());
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
        assertEquals(13, allAppointments.size());
        Appointment apt = new Appointment();
        apt.setPatient(allAppointments.get(0).getPatient());
        appointmentDao.save(apt);
        allAppointments = appointmentDao.getAllAppointments(null);
        assertEquals(14, allAppointments.size());
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
        assertEquals(3, allFutureAppointmentsForServiceType.size());
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
        assertEquals(2, allAppointments.size());
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
        assertEquals(13, allAppointmentServices.size());
    }

    @Test
    public void shouldSearchAppointmentsForAPatient() {
        // TODO: #92
        // this test fails after adding a default value in Appointment:
        // private Boolean isTeleconsultationEnabled = Boolean.FALSE;
        // fix that in the search function (not test)
        List<Appointment> allAppointments = appointmentDao.getAllAppointments(null);
        Appointment appointment = new Appointment();
        appointment.setPatient(allAppointments.get(0).getPatient());
        appointment.setLocation(allAppointments.get(0).getLocation());
        appointment.setService(allAppointments.get(0).getService());
        appointment.setStatus(null);
        List<Appointment> searchedAppointmentList = appointmentDao.search(appointment);
        assertEquals(3, searchedAppointmentList.size());
    }

    @Test
    public void shouldReturnAllAppointmentsBetweenGivenDatesWithoutLimiting() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2108-08-10T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDate = DateUtil.convertToDate("2108-08-16T18:29:59.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(6, appointments.size());
    }

    @Test
    public void shouldReturnLimitedAppointmentsIfEndDateIsNotPassed() throws Exception {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2000-08-10T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(10, appointments.size());
    }

    @Test
    public void shouldReturnAllAppointmentsBetweenGivenDatesForLimit() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2008-08-10T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDate = DateUtil.convertToDate("2120-08-16T18:29:59.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);
        appointmentSearchRequest.setLimit(2);

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(2, appointments.size());
    }

    @Test
    public void shouldReturnAllAppointmentsBetweenGivenDatesInSortedOrder() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2008-08-10T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDate = DateUtil.convertToDate("2120-08-16T18:29:59.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);
        appointmentSearchRequest.setPatientUuid("75e04d42-3ca8-11e3-bf2b-0800271c1b78");

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(2, appointments.size());
        assertEquals(DateUtil.convertToDate("2108-08-08T13:00:00.0Z", DateUtil.DateFormatType.UTC)
                , appointments.get(0).getStartDateTime());
        assertEquals(DateUtil.convertToDate("2108-08-11T13:00:00.0Z", DateUtil.DateFormatType.UTC)
                , appointments.get(1).getStartDateTime());
    }

    @Test
    public void shouldReturnAppointmentsForPatientBetweenGivenDates() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2108-08-10T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDate = DateUtil.convertToDate("2108-08-16T18:29:59.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);
        appointmentSearchRequest.setPatientUuid("75e04d42-3ca8-11e3-bf2b-0800271c1b78");

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(1, appointments.size());
    }

    @Test
    public void shouldReturnAppointmentsForProvider() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        appointmentSearchRequest.setProviderUuid("2d15071d-439d-44e8-9825-aa8e1a30d2a2");

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(1, appointments.size());
    }

    @Test
    public void shouldReturnAppointmentsForPatientBetweenGivenDatesForASpecificStatus() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = DateUtil.convertToDate("2108-08-10T18:30:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDate = DateUtil.convertToDate("2108-08-16T18:29:59.0Z", DateUtil.DateFormatType.UTC);
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);
        appointmentSearchRequest.setStatus(AppointmentStatus.Scheduled);

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(3, appointments.size());
    }

    @Test
    public void shouldReturnAllAppointmentsInNoGivenDates() throws ParseException {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();

        List<Appointment> appointments = appointmentDao.search(appointmentSearchRequest);

        assertEquals(13, appointments.size());
    }

    @Test
    public void shouldReturnAllNonVoidedFutureAppointmentsOfPatient() {
        List<Appointment> appointments = appointmentDao.getAppointmentsForPatient(1);
        assertEquals(5, appointments.size());
    }

    @Test
    public void shouldReturnEmptyListWhenPatientIsNull() {
        List<Appointment> appointments = appointmentDao.getAppointmentsForPatient(null);
        assertNotNull(appointments);
        assertEquals(0, appointments.size());
    }

    @Test
    public void testGetAllAppointments() {
        Date forDate = new Date();
        List<Appointment> appointments = appointmentDao.getAllAppointments(forDate);
        assertNotNull(appointments);
        assertEquals(0,appointments.size());
    }

    @Test
    public void testGetAllAppointmentsReminder() {
        String hours = "24";
        List<Appointment> result = appointmentDao.getAllAppointmentsReminder(hours);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearch() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        appointmentSearchRequest.setStartDate(new Date());
        appointmentSearchRequest.setEndDate(new Date());
        List<Appointment> result = appointmentDao.search(appointmentSearchRequest);

        assertEquals(0, result.size());
    }

    public void shouldReturnDateLessAppointments() {
        AppointmentSearchRequestModel searchQuery = new AppointmentSearchRequestModel();
        List<Appointment> appointments = appointmentDao.getDatelessAppointments(searchQuery);
        assertNotNull(appointments);
        assertEquals(3, appointments.size());
    }

    @Test
    public void shouldReturnAppointmentsBasedOnAppointmentSearchRequestModelPatientAndLocation() {

        List<Appointment> allAppointments = appointmentDao.getAllAppointments(null);
        AppointmentSearchRequestModel searchQuery = new AppointmentSearchRequestModel();
        List<String> patientUuids=new ArrayList<>();
        patientUuids.add(allAppointments.get(0).getPatient().getUuid());
        List<String> locationUuids=new ArrayList<>();
        locationUuids.add(allAppointments.get(0).getLocation().getUuid());

        searchQuery.setPatientUuids(patientUuids);
        searchQuery.setLocationUuids(locationUuids);
        searchQuery.setStatus(null);

        List<Appointment> appointments = appointmentDao.search(searchQuery);

        assertNotNull(appointments);
        assertEquals(3, appointments.size());
    }

    @Test
    public void shouldReturnAppointmentsBasedOnAppointmentSearchRequestModelServiceAndServiceType() {

        List<Appointment> allAppointments = appointmentDao.getAllAppointments(null);
        AppointmentSearchRequestModel searchQuery = new AppointmentSearchRequestModel();
        List<String> serviceUuids=new ArrayList<>();
        serviceUuids.add(allAppointments.get(0).getService().getUuid());
        List<String> serviceTypeUuids=new ArrayList<>();
        serviceTypeUuids.add(allAppointments.get(0).getServiceType().getUuid());

        searchQuery.setServiceUuids(serviceUuids);
        searchQuery.setServiceTypeUuids(serviceTypeUuids);
        searchQuery.setStatus(null);

        List<Appointment> appointments = appointmentDao.search(searchQuery);

        assertNotNull(appointments);
        assertEquals(2, appointments.size());
    }

    @Test
    public void shouldReturnAppointmentsBasedOnAppointmentSearchRequestModelPriority() {

        List<Appointment> allAppointments = appointmentDao.getAllAppointments(null);
        AppointmentSearchRequestModel searchQuery = new AppointmentSearchRequestModel();
        List<String> priorities=new ArrayList<>();
        priorities.add("AsNeeded");

        searchQuery.setPriorities(priorities);
        searchQuery.setStatus(null);

        List<Appointment> appointments = appointmentDao.search(searchQuery);

        assertNotNull(appointments);
        assertEquals(1, appointments.size());
    }

    @Test
    public void shouldReturnEmptyAppointmentsBasedOnAppointmentSearchRequestModelProviderWhenNoAppointmentsAreAssignedToProvider() {

        AppointmentSearchRequestModel searchQuery = new AppointmentSearchRequestModel();
        List<String> providerUuids=new ArrayList<>();
        providerUuids.add(UUID.randomUUID().toString());

        searchQuery.setProviderUuids(providerUuids);
        searchQuery.setStatus(null);

        List<Appointment> appointments = appointmentDao.search(searchQuery);

        assertNotNull(appointments);
        assertEquals(0, appointments.size());
    }
}
