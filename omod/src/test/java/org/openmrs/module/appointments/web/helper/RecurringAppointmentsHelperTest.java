package org.openmrs.module.appointments.web.helper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;

public class RecurringAppointmentsHelperTest {

    private RecurringAppointmentsHelper recurringAppointmentsHelper;

    private AppointmentMapper appointmentMapperMock;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        appointmentMapperMock = Mockito.mock(AppointmentMapper.class);
        recurringAppointmentsHelper = new RecurringAppointmentsHelper(appointmentMapperMock);
    }

    @Test
    public void shouldReturnAppointmentsForGivenDatesAndAppointmentRequest() throws ParseException {
        Date appointmentDateOne = getDate(2019, Calendar.MAY, 13);
        Date appointmentDateTwo = getDate(2019, Calendar.MAY, 16);

        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setStartDateTime(appointmentStartDateTime);
        appointmentRequest.setEndDateTime(appointmentEndDateTime);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = recurringAppointmentsHelper.generateAppointments(Arrays.asList(appointmentDateOne,
                appointmentDateTwo), appointmentRequest);

        assertEquals(2, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());

        assertEquals(getDate(2019, Calendar.MAY, 16, 16, 00, 00).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 16, 16, 30, 00).toString(),
                appointments.get(1).getEndDateTime().toString());

    }

    @Test
    public void shouldThrowExceptionWhenTypeIsNullInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    private RecurringPattern getRecurringPattern() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("DAY");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(1);
        return recurringPattern;
    }

    @Test
    public void shouldThrowExceptionWhenTypeIsEmptyInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("");
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenPeriodIsLessThanOneInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setPeriod(0);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    private String getExceptionMessage() {
        return "type should be DAY/WEEK\n" +
                "period and frequency/endDate are mandatory if type is DAY\n" +
                "daysOfWeek and frequency/endDate are mandatory if type is WEEK";
    }

    @Test
    public void shouldThrowExceptionWhenFrequencyIsLessThanOneInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setFrequency(0);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

}
