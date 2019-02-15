package org.openmrs.module.appointments.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;



public class AppointmentTest {

    @Test
    public void shouldGetProvidersWhoAccepted() {
        Appointment appointment = getAppointment();
        Set<AppointmentProvider> providersWithResponse =
                appointment.getProvidersWithResponse(AppointmentProviderResponse.ACCEPTED);
        Assert.assertEquals(2, providersWithResponse.size());
        Assert.assertTrue(providersWithResponse.stream().anyMatch(provider -> "1DummyUuidJustForTest".equals(provider.getUuid())));
        Assert.assertTrue(providersWithResponse.stream().anyMatch(provider -> "2DummyUuidJustForTest".equals(provider.getUuid())));
    }

    @Test
    public void shouldGetProvidersWhoRejected() {
        Appointment appointment = getAppointment();
        Set<AppointmentProvider> providersWithResponse =
                appointment.getProvidersWithResponse(AppointmentProviderResponse.REJECTED);
        Assert.assertEquals(1, providersWithResponse.size());
        Assert.assertTrue(providersWithResponse.stream().anyMatch(provider -> "3DummyUuidJustForTest".equals(provider.getUuid())));
    }

    private Appointment getAppointment() {
        Appointment appointment = new Appointment();
        Set<AppointmentProvider> providers = new HashSet<>();
        AppointmentProvider ap1 = new AppointmentProvider();
        ap1.setUuid("1DummyUuidJustForTest");
        ap1.setResponse(AppointmentProviderResponse.ACCEPTED);
        providers.add(ap1);

        AppointmentProvider ap2 = new AppointmentProvider();
        ap2.setUuid("2DummyUuidJustForTest");
        ap2.setResponse(AppointmentProviderResponse.ACCEPTED);
        providers.add(ap2);

        AppointmentProvider ap3 = new AppointmentProvider();
        ap3.setUuid("3DummyUuidJustForTest");
        ap3.setResponse(AppointmentProviderResponse.REJECTED);
        providers.add(ap3);

        appointment.setProviders(providers);
        return appointment;
    }

}