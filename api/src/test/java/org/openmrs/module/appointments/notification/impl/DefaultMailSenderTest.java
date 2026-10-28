package org.openmrs.module.appointments.notification.impl;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMailSenderTest {

    @Mock
    AdministrationService administrationService;

    private MockedStatic<Context> contextMock;

    @Before
    public void init() {
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
        contextMock.when(() -> Context.addProxyPrivilege(anyString())).thenAnswer(invocation -> null);
        contextMock.when(() -> Context.removeProxyPrivilege(anyString())).thenAnswer(invocation -> null);
    }

    @After
    public void tearDown() {
        contextMock.close();
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

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error occurred while sending email");
        expectedEx.expectCause(instanceOf(java.lang.NullPointerException.class));
        mailSender.send("test", "nothing", new String[] {""}, null, null);
    }

}
