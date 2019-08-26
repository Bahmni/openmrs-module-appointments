package org.openmrs.module.appointments.web.service.impl;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.helper.RecurringPatternHelper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;
import java.text.ParseException;
import java.util.*;
import static org.junit.Assert.*;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;
public class DailyRecurringAppointmentsGenerationServiceTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AbstractRecurringAppointmentsGenerationService abstractRecurringAppointmentsGenerationService;
    private AppointmentMapper appointmentMapperMock;
    private RecurringPatternHelper recurringPatternHelper = new RecurringPatternHelper(appointmentMapperMock);
    @Before
    public void setUp() {
        appointmentMapperMock = Mockito.mock(AppointmentMapper.class);
    }

    private AppointmentRequest getAppointmentRequest(Date appointmentStartDateTime, Date appointmentEndDateTime) {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setStartDateTime(appointmentStartDateTime);
        appointmentRequest.setEndDateTime(appointmentEndDateTime);
        return appointmentRequest;
    }

    private AppointmentRecurringPattern getAppointmentRecurringPattern(int period, int frequency, Date endDate) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setPeriod(period);
        appointmentRecurringPattern.setFrequency(frequency);
        appointmentRecurringPattern.setEndDate(endDate);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        return appointmentRecurringPattern;
    }

    @Test
    public void shouldReturnAppointmentsForGivenRecurringPatternAndAppointmentRequest() throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3,
                2, null);
        abstractRecurringAppointmentsGenerationService = new DailyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);
        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = abstractRecurringAppointmentsGenerationService.getAppointments();
        assertEquals(2, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 16, 16, 00, 00).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 16, 16, 30, 00).toString(),
                appointments.get(1).getEndDateTime().toString());
    }

    @Test
    public void shouldReturnAppointmentsAcrossDaysForGivenRecurringPatternAndAppointmentRequest() throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 16, 23, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 17, 00, 15, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 2,
                null);
        abstractRecurringAppointmentsGenerationService = new DailyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);
        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = abstractRecurringAppointmentsGenerationService.getAppointments();
        assertEquals(2, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 23, 45, 0).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 20, 0, 15, 0).toString(),
                appointments.get(1).getEndDateTime().toString());
    }

    @Test
    public void shouldReturnAppointmentsWithSlotStartsAndEndsOnDifferentDatesForGivenRecurringPatternWithEndDate() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 16, 23, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 17, 00, 15, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 23, 45, 00));
        abstractRecurringAppointmentsGenerationService = new DailyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);
        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = abstractRecurringAppointmentsGenerationService.getAppointments();
        assertEquals(4, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 23, 45, 0).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 20, 0, 15, 0).toString(),
                appointments.get(1).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 22, 23, 45, 0).toString(),
                appointments.get(2).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 23, 0, 15, 0).toString(),
                appointments.get(2).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 25, 23, 45, 0).toString(),
                appointments.get(3).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 26, 0, 15, 0).toString(),
                appointments.get(3).getEndDateTime().toString());
    }

    @Test
    public void shouldReturnAppointmentsForGivenRecurringPatternWithEndDate() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 16, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 16, 9, 15, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 8, 45, 00));
        abstractRecurringAppointmentsGenerationService = new DailyRecurringAppointmentsGenerationService(appointmentRecurringPattern, appointmentRequest, appointmentMapperMock);
        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = abstractRecurringAppointmentsGenerationService.getAppointments();
        assertEquals(4, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 8, 45, 0).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 9, 15, 0).toString(),
                appointments.get(1).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 22, 8, 45, 0).toString(),
                appointments.get(2).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 22, 9, 15, 0).toString(),
                appointments.get(2).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 25, 8, 45, 0).toString(),
                appointments.get(3).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 25, 9, 15, 0).toString(),
                appointments.get(3).getEndDateTime().toString());
    }
}