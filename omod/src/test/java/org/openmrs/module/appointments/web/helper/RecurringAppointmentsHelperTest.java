package org.openmrs.module.appointments.web.helper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;

public class RecurringAppointmentsHelperTest {

    private RecurringAppointmentsHelper recurringAppointmentsHelper;

    private AppointmentMapper appointmentMapperMock;

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
    public void shouldReturnFalseWhenTypeIsNullInAppointmentRequest() {
        RecurringPattern recurringPattern = new RecurringPattern();
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPattern));
    }

    @Test
    public void shouldReturnTrueWhenRecurringPatternHavingAllData() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(1);
        recurringPattern.setPeriod(1);
        recurringPattern.setType("DAY");
        assertTrue(recurringAppointmentsHelper.validateRecurringPattern(recurringPattern));
    }

    @Test
    public void shouldReturnFalseWhenPeriodAndFrequencyIsLessThanOneOrNullInAppointmentRequest() {
        RecurringPattern recurringPatternOne = new RecurringPattern();
        recurringPatternOne.setPeriod(0);
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPatternOne));

        RecurringPattern recurringPatternTwo = new RecurringPattern();
        recurringPatternTwo.setFrequency(0);
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPatternTwo));

        RecurringPattern recurringPatternThree = new RecurringPattern();
        recurringPatternThree.setType("");
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPatternThree));
    }

    @Test
    public void shouldReturnFalseWhenTypeIsBlankThanOneInAppointmentRequest() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(1);
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPattern));
    }

    @Test
    public void shouldReturnFalseWhenFrequencyIsLessThanOneInAppointmentRequest() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("DAY");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(0);
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPattern));
    }

    @Test
    public void shouldReturnFalseWhenPeriodIsLessThanOneInAppointmentRequest() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("WEEK");
        recurringPattern.setPeriod(0);
        recurringPattern.setFrequency(1);
        assertFalse(recurringAppointmentsHelper.validateRecurringPattern(recurringPattern));
    }
}
