package org.openmrs.module.appointments.service.impl;

import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.model.SearchRequestMeta;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
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
        mockDaoReturns(Collections.<Appointment>emptyList());

        searchService.search(request);

        verify(validator, times(1)).validateRequest(request);
    }

    @Test
    public void shouldDelegateToDaoWithRequestCriteria() {
        AppointmentSearchRequest request = validRequest();
        mockDaoReturns(Collections.<Appointment>emptyList());

        searchService.search(request);

        verify(appointmentSearchDao, times(1)).search(
                eq(request.getCriteria()), (Long) isNull(), anyString(), (String) isNull(), eq(101));
    }

    @Test
    public void shouldReturnSuccessResponseWithEmptyResultsWhenNoAppointmentsFound() {
        AppointmentSearchRequest request = validRequest();
        mockDaoReturns(Collections.<Appointment>emptyList());

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
        firstAppointment.setAppointmentId(1);
        Appointment secondAppointment = new Appointment();
        secondAppointment.setAppointmentId(2);
        List<Appointment> appointments = Arrays.asList(firstAppointment, secondAppointment);
        mockDaoReturns(appointments);

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
        mockDaoReturns(Collections.<Appointment>emptyList());

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.getContext(), is("appointment"));
    }

    @Test
    public void shouldReturnHasMoreTrueWhenDaoReturnsMoreThanLimit() {
        AppointmentSearchRequest request = requestWithPagination(2, null, null);
        Appointment a1 = appointmentWithId(10);
        Appointment a2 = appointmentWithId(9);
        Appointment a3 = appointmentWithId(8); // extra = hasMore
        mockDaoReturns(Arrays.asList(a1, a2, a3));
        when(responseBuilder.mapAppointment(any(Appointment.class))).thenReturn(new HashMap<String, Object>());

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.getResults().size(), is(2));
        assertThat(response.getMeta().getPagination().getNextCursor(), is(notNullValue()));
    }

    @Test
    public void shouldReturnNullNextCursorWhenNoMoreResults() {
        AppointmentSearchRequest request = requestWithPagination(5, null, null);
        Appointment a1 = appointmentWithId(10);
        Appointment a2 = appointmentWithId(9);
        mockDaoReturns(Arrays.asList(a1, a2));
        when(responseBuilder.mapAppointment(any(Appointment.class))).thenReturn(new HashMap<String, Object>());

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.getResults().size(), is(2));
        assertThat(response.getMeta().getPagination().getNextCursor(), is(nullValue()));
    }

    @Test
    public void shouldDecodeCursorAndPassToDao() {
        String cursor = CursorCodec.encode(50);
        AppointmentSearchRequest request = requestWithPagination(10, cursor, "next");

        mockDaoReturns(Collections.<Appointment>emptyList());

        searchService.search(request);

        verify(appointmentSearchDao, times(1)).search(
                any(SearchCondition.class), eq(50L), anyString(), eq("next"), eq(11));
    }

    @Test
    public void shouldCapLimitToMax500() {
        AppointmentSearchRequest request = requestWithPagination(1000, null, null);
        mockDaoReturns(Collections.<Appointment>emptyList());

        searchService.search(request);

        verify(appointmentSearchDao, times(1)).search(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(501));
    }

    @Test
    public void shouldCountPatientsWhenIncludeTotalCountIsTrue() {
        AppointmentSearchRequest request = validRequest();
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setIncludeTotalCount(true);
        request.setMeta(meta);
        mockDaoReturns(Collections.<Appointment>emptyList());
        when(appointmentSearchDao.count(any(SearchCondition.class))).thenReturn(42L);

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.getMeta().getTotalCount(), is(42L));
        verify(appointmentSearchDao, times(1)).count(any(SearchCondition.class));
    }

    @Test
    public void shouldNotCountWhenIncludeTotalCountIsFalse() {
        AppointmentSearchRequest request = validRequest();
        mockDaoReturns(Collections.<Appointment>emptyList());

        AppointmentSearchResponse response = searchService.search(request);

        assertThat(response.getMeta().getTotalCount(), is(nullValue()));
        verify(appointmentSearchDao, never()).count(any(SearchCondition.class));
    }

    private void mockDaoReturns(List<Appointment> appointments) {
        when(appointmentSearchDao.search(
                any(SearchCondition.class), any(), anyString(), any(), anyInt()))
                .thenReturn(appointments);
    }

    private Appointment appointmentWithId(int id) {
        Appointment a = new Appointment();
        a.setAppointmentId(id);
        return a;
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

    private AppointmentSearchRequest requestWithPagination(int limit, String cursor, String direction) {
        AppointmentSearchRequest request = validRequest();
        SearchRequestMeta meta = new SearchRequestMeta();
        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(limit);
        pagination.setCursor(cursor);
        pagination.setDirection(direction);
        meta.setPagination(pagination);
        request.setMeta(meta);
        return request;
    }
}
