package org.openmrs.module.appointments.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.appointments.properties.AppointmentProperties;

import java.io.*;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public class AppointmentVisitLocationTest {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @InjectMocks
    private AppointmentVisitLocationImpl appointmentVisitLocation;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Properties properties = new Properties();
        properties.setProperty("openmrs.root.uri", "http://example.com/openmrs");
        AppointmentProperties.setProperties(properties);
        appointmentVisitLocation.setBaseURL("http://example.com/openmrs");
    }

    @Test
    public void shouldGetFacilityName() throws IOException {
        String locationUuid = "12345";

        BufferedReader reader = mock(BufferedReader.class);
        String facilityName = appointmentVisitLocation.getFacilityName(locationUuid);
    }
}