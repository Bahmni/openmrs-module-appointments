package org.openmrs.module.appointments.service.impl;

import org.bahmni.search.model.SearchCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.builder.AppointmentResponseBuilder;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;
import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.validation.CriteriaValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentSearchServiceImplTest {

    @Mock
    private AppointmentSearchDao appointmentSearchDao;

    @Mock
    private CriteriaValidator validator;

    @Mock
    private AppointmentResponseBuilder responseBuilder;

    private AppointmentSearchServiceImpl searchService;

    @Before
    public void setUp() {
        searchService = new AppointmentSearchServiceImpl(appointmentSearchDao, validator, responseBuilder);
    }

    @Test
    public void shouldValidateRequestBeforeSearching() {
        AppointmentSearchRequest request = validRequest();
        when(appointmentSearchDao.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

        searchService.search(request);

        verify(validator, times(1)).validateRequest(request);
    }

    @Test
    public void shouldDelegateToDaoWithRequestCriteria() {
        AppointmentSearchRequest request = validRequest();
        when(appointmentSearchDao.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

        searchService.search(request);

        verify(appointmentSearchDao, times(1)).search(eq(request.getCriteria()));
    }

    @Test
    public void shouldReturnSuccessResponseWithEmptyResultsWhenNoAppointmentsFound() {
        AppointmentSearchRequest request = validRequest();
        when(appointmentSearchDao.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.isSuccess(), is(true));
        assertThat(response.getContext(), is(AppointmentSearchConstants.ENTITY_APPOINTMENT));
        assertThat(response.getResults().size(), is(0));
        verify(responseBuilder, never()).mapAppointment(any(Appointment.class));
    }

    @Test
    public void shouldMapEachAppointmentReturnedByDaoIntoResponseResults() {
        AppointmentSearchRequest request = validRequest();
        Appointment firstAppointment = new Appointment();
        Appointment secondAppointment = new Appointment();
        List<Appointment> appointments = Arrays.asList(firstAppointment, secondAppointment);
        when(appointmentSearchDao.search(any(SearchCondition.class))).thenReturn(appointments);

        Map<String, Object> firstMap = new HashMap<>();
        firstMap.put("uuid", "apt-uuid-1");
        Map<String, Object> secondMap = new HashMap<>();
        secondMap.put("uuid", "apt-uuid-2");
        when(responseBuilder.mapAppointment(firstAppointment)).thenReturn(firstMap);
        when(responseBuilder.mapAppointment(secondAppointment)).thenReturn(secondMap);

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.isSuccess(), is(true));
        assertThat(response.getResults().size(), is(2));
        assertThat(response.getResults().get(0), is(firstMap));
        assertThat(response.getResults().get(1), is(secondMap));
        verify(responseBuilder, times(1)).mapAppointment(firstAppointment);
        verify(responseBuilder, times(1)).mapAppointment(secondAppointment);
    }

    @Test
    public void shouldReturnResponseWithAppointmentEntityContext() {
        AppointmentSearchRequest request = validRequest();
        when(appointmentSearchDao.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.getContext(), is("appointment"));
    }

    private AppointmentSearchRequest validRequest() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setEntity(AppointmentSearchConstants.ENTITY_APPOINTMENT);
        SearchCondition criteria = new SearchCondition();
        criteria.setField("appointment.startDate");
        criteria.setComparator("gt");
        criteria.setValue("2024-01-01T00:00:00.000+0000");
        request.setCriteria(criteria);
        return request;
    }
}
