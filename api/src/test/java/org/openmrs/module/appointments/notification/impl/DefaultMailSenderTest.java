package org.openmrs.module.appointments.notification.impl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class DefaultMailSenderTest {

    @Mock
    AdministrationService administrationService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }
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

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mailSender.send("test", "nothing", new String[] {""}, null, null);
        });
        assertThat(exception.getMessage(), containsString("Error occurred while sending email"));
        assertThat(exception.getCause(), instanceOf(java.lang.NullPointerException.class));
    }

}
