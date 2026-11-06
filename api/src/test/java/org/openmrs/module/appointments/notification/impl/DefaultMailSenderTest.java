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

import javax.mail.internet.AddressException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMailSenderTest {

    @Mock
    AdministrationService administrationService;

    private MockedStatic<Context> contextMock;

    private final Map<String, String> globalProperties = new HashMap<>();

    @Before
    public void init() {
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
        contextMock.when(() -> Context.addProxyPrivilege(anyString())).thenAnswer(invocation -> null);
        contextMock.when(() -> Context.removeProxyPrivilege(anyString())).thenAnswer(invocation -> null);

        // Any global property that a test does not set resolves to the default the code asks for,
        // as it would in a real OpenMRS (a bare mock would return null and break Properties.put).
        when(administrationService.getGlobalProperty(anyString(), anyString()))
                .thenAnswer(invocation -> globalProperties.getOrDefault(
                        invocation.<String>getArgument(0), invocation.<String>getArgument(1)));
        globalProperties.put("mail.smtp_host", "localhost");
        globalProperties.put("mail.smtp.ssl.enable", "false");
    }

    @After
    public void tearDown() {
        contextMock.close();
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldNotSendWhenMailSendGlobalPropertyIsNotEnabled() {
        // mail.send defaults to false in the global property fallback, so this must be a silent no-op:
        // no sender address, no recipient validation and no connection attempt.
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        mailSender.send("test", "nothing", null, null, null);
    }

    @Test
    public void shouldHonourMailSendGlobalPropertyInFallbackConfiguration() {
        globalProperties.put("mail.send", "true");
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        // Getting past the "sending disabled" check proves mail.send was read from the global property.
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error occurred while sending email");
        expectedEx.expectCause(allOf(instanceOf(IllegalStateException.class),
                hasProperty("message", containsString("Sender address is not configured"))));
        mailSender.send("test", "nothing", new String[]{"patient@example.com"}, null, null);
    }

    @Test
    public void shouldThrowClearErrorWhenThereIsNoToRecipient() {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error occurred while sending email");
        expectedEx.expectCause(allOf(instanceOf(IllegalArgumentException.class),
                hasProperty("message", containsString("At least one 'to' recipient"))));
        mailSender.send("test", "nothing", null, null, null);
    }

    @Test
    public void shouldThrowClearErrorWhenToRecipientsAreEmpty() {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectCause(instanceOf(IllegalArgumentException.class));
        mailSender.send("test", "nothing", new String[0], null, null);
    }

    @Test
    public void shouldThrowErrorForInvalidEmailAddress() {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error occurred while sending email");
        expectedEx.expectCause(instanceOf(AddressException.class));
        mailSender.send("test", "nothing", new String[]{""}, null, null);
    }

}
