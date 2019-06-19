package org.openmrs.module.appointments.web.helper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;

public class RecurringAppointmentsHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private RecurringAppointmentsHelper recurringAppointmentsHelper;
    private AppointmentMapper appointmentMapperMock;

    @Before
    public void setUp() {
        appointmentMapperMock = Mockito.mock(AppointmentMapper.class);
        recurringAppointmentsHelper = new RecurringAppointmentsHelper(appointmentMapperMock);
    }

    private RecurringPattern getRecurringPattern() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("DAY");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(1);
        return recurringPattern;
    }

    @Test
    public void shouldThrowExceptionWhenTypeIsNullInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
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
    public void shouldThrowExceptionWhenInvalidType() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("TEXT");
        recurringPattern.setDaysOfWeek(null);
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

    @Test
    public void shouldThrowExceptionWhenFrequencyIsLessThanOneInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setFrequency(0);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenFrequencyAndEndDateAreNullInRecurringPattern() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setFrequency(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    private String getExceptionMessage() {
        return "type should be DAY/WEEK\n" +
                "period and frequency/endDate are mandatory if type is DAY\n" +
                "daysOfWeek, period and frequency/endDate are mandatory if type is WEEK";
    }

    @Test
    public void shouldThrowExceptionWhenEndDateIsNull() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setEndDate(null);
        recurringPattern.setFrequency(0);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenWeekTypeWithEmptyDays() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("WEEk");
        recurringPattern.setEndDate(null);
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(Arrays.asList());
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenWeekTypeWithNoDays() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("WEEk");
        recurringPattern.setEndDate(null);
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenWeekTypeWithInvalidDays() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("WEEk");
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(Arrays.asList("TEXT", "MISC"));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenWeekTypeWithNoPeriod() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("WEEk");
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(0);
        recurringPattern.setDaysOfWeek(Arrays.asList("SUNDAY", "MONDAY"));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldThrowExceptionWhenWeekTypeWithNoFrequency() {
        RecurringPattern recurringPattern = getRecurringPattern();
        recurringPattern.setType("WEEk");
        recurringPattern.setFrequency(0);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(Arrays.asList("SUNDAY", "MONDAY"));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(getExceptionMessage());
        recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
    }

    @Test
    public void shouldNotThrowAnExceptionForValidData() {
        RecurringPattern recurringPattern = getRecurringPattern();
        assertTrue(recurringAppointmentsHelper.validateRecurringPattern(recurringPattern));
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
    public void shouldReturnDailyRecurringAppointmentsForDayType() {
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1, null);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setFrequency(2);
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = recurringAppointmentsHelper
                .generateRecurringAppointments(appointmentRecurringPattern, appointmentRequest);

        assertEquals(appointments.size(), appointments.size());

        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnDailyRecurringAppointmentsForWeekType() {
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1, null);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setFrequency(2);
        appointmentRecurringPattern.setType(RecurringAppointmentType.WEEK);
        appointmentRecurringPattern.setDaysOfWeek("SUNDAY,MONDAY");

        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 19, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 19, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 26, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 26, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = recurringAppointmentsHelper
                .generateRecurringAppointments(appointmentRecurringPattern, appointmentRequest);

        assertEquals(appointments.size(), appointments.size());

        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnDailyRecurringAppointmentsForNullType() {
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1, null);
        appointmentRecurringPattern.setType(null);

        AppointmentRequest appointmentRequest = getAppointmentRequest(null, null);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = recurringAppointmentsHelper
                .generateRecurringAppointments(appointmentRecurringPattern, appointmentRequest);
        assertTrue(appointments.isEmpty());
    }

}