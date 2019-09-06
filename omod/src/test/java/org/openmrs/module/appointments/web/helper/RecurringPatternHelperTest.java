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
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
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

public class RecurringPatternHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private RecurringPatternHelper recurringPatternHelper;
    private AppointmentMapper appointmentMapperMock;

    @Before
    public void setUp() {
        appointmentMapperMock = Mockito.mock(AppointmentMapper.class);
        recurringPatternHelper = new RecurringPatternHelper(appointmentMapperMock);
    }

    private RecurringAppointmentRequest getAppointmentRequest(Date appointmentStartDateTime, Date appointmentEndDateTime) {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointmentStartDateTime);
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointmentEndDateTime);
        return recurringAppointmentRequest;
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
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);

        Mockito.when(appointmentMapperMock.fromRequest(recurringAppointmentRequest.getAppointmentRequest()))
                .thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = new ArrayList<>(recurringPatternHelper
                .generateRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest));

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
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);

        Mockito.when(appointmentMapperMock.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = new ArrayList<>(recurringPatternHelper
                .generateRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest));

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

        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(null, null);

        Mockito.when(appointmentMapperMock.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = new ArrayList<>(recurringPatternHelper
                .generateRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest));
        assertTrue(appointments.isEmpty());
    }

}
