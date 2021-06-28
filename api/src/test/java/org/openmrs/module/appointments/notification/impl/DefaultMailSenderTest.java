package org.openmrs.module.appointments.notification.impl;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class DefaultMailSenderTest {

    @Mock
    AdministrationService administrationService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldThrowErrorForInvalidEmailAddress() {
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        when(administrationService.getGlobalProperty(eq("mail.transport_protocol"), eq("smtp"))).thenReturn("smtp");
        //when(administrationService.getGlobalProperty(any(), any())).thenReturn("smtp");
        when(administrationService.getGlobalProperty("mail.smtp_host", "")).thenReturn("localhost");
        when(administrationService.getGlobalProperty("mail.smtp_port", "25")).thenReturn("25");
        when(administrationService.getGlobalProperty("mail.smtp_auth", "false")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.debug", "false")).thenReturn("false");
        when(administrationService.getGlobalProperty("mail.from", "")).thenReturn("noreply@bahmni.org");
        when(administrationService.getGlobalProperty("mail.user", "")).thenReturn("test");
        when(administrationService.getGlobalProperty("mail.password", "")).thenReturn("random");

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error occurred while sending email");
        expectedEx.expectCause(instanceOf(javax.mail.internet.AddressException.class));
        mailSender.send("test", "nothing", new String[] {""}, null, null);
    }

}
