package org.openmrs.module.appointments.notification.impl;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.util.OpenmrsUtil;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMailSenderTest {

    private static final String[] TO = {"patient@example.com"};

    @Mock
    AdministrationService administrationService;

    @Mock
    Transport transport;

    @Rule
    public TemporaryFolder applicationDataDirectory = new TemporaryFolder();

    private MockedStatic<Context> contextMock;
    private MockedStatic<OpenmrsUtil> openmrsUtilMock;

    private final Map<String, String> globalProperties = new HashMap<>();

    @Before
    public void init() {
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
        contextMock.when(() -> Context.addProxyPrivilege(anyString())).thenAnswer(invocation -> null);
        contextMock.when(() -> Context.removeProxyPrivilege(anyString())).thenAnswer(invocation -> null);

        // Use an empty temporary folder, so the tests never pick up a real mail-config.properties.
        openmrsUtilMock = Mockito.mockStatic(OpenmrsUtil.class, Mockito.CALLS_REAL_METHODS);
        openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory)
                .thenReturn(applicationDataDirectory.getRoot().getAbsolutePath());

        // Any global property that a test does not set resolves to the default the code asks for,
        // as it would in a real OpenMRS (a bare mock would return null and break Properties.put).
        // Lenient because tests that read mail-config.properties never touch global properties.
        lenient().when(administrationService.getGlobalProperty(anyString(), anyString()))
                .thenAnswer(invocation -> globalProperties.getOrDefault(
                        invocation.<String>getArgument(0), invocation.<String>getArgument(1)));
        globalProperties.put("mail.smtp_host", "localhost");
        globalProperties.put("mail.smtp.ssl.enable", "false");
    }

    @After
    public void tearDown() {
        openmrsUtilMock.close();
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
        mailSender.send("test", "nothing", TO, null, null);
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

    @Test
    public void shouldSendThroughTransportAndCloseIt() throws Exception {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        DefaultMailSender mailSender = mailSenderUsingTransport();

        mailSender.send("test", "nothing", TO, new String[]{"cc@example.com"}, new String[]{"bcc@example.com"});

        verify(transport).connect("localhost", "", "");
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<Address[]> recipients = ArgumentCaptor.forClass(Address[].class);
        verify(transport).sendMessage(sent.capture(), recipients.capture());
        assertEquals("test", sent.getValue().getSubject());
        assertEquals(3, recipients.getValue().length);
        verify(transport).close();
    }

    @Test
    public void shouldCloseTransportWhenSendingFails() throws Exception {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        doThrow(new MessagingException("smtp down")).when(transport).sendMessage(any(Message.class), any(Address[].class));
        DefaultMailSender mailSender = mailSenderUsingTransport();

        try {
            mailSender.send("test", "nothing", TO, null, null);
            fail("Expected the send failure to be reported");
        } catch (RuntimeException e) {
            assertEquals("Error occurred while sending email", e.getMessage());
            assertEquals("smtp down", e.getCause().getMessage());
        }

        verify(transport).close();
    }

    @Test
    public void shouldReportTheSendFailureWhenClosingTheTransportAlsoFails() throws Exception {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        doThrow(new MessagingException("smtp down")).when(transport).sendMessage(any(Message.class), any(Address[].class));
        doThrow(new MessagingException("close failed")).when(transport).close();
        DefaultMailSender mailSender = mailSenderUsingTransport();

        try {
            mailSender.send("test", "nothing", TO, null, null);
            fail("Expected the send failure to be reported");
        } catch (RuntimeException e) {
            assertEquals("smtp down", e.getCause().getMessage());
        }
    }

    @Test
    public void shouldNotFailWhenOnlyClosingTheTransportFails() throws Exception {
        globalProperties.put("mail.send", "true");
        globalProperties.put("mail.from", "noreply@example.com");
        doThrow(new MessagingException("close failed")).when(transport).close();
        DefaultMailSender mailSender = mailSenderUsingTransport();

        mailSender.send("test", "nothing", TO, null, null);

        verify(transport).sendMessage(any(Message.class), any(Address[].class));
    }

    @Test
    public void shouldUseSettingsAndSenderFromMailConfigFileInsteadOfGlobalProperties() throws Exception {
        writeMailConfigFile("mail.send=true", "mail.from=file-sender@example.com", "mail.smtp.host=smtp.file.example");
        globalProperties.put("mail.from", "gp-sender@example.com");
        DefaultMailSender mailSender = mailSenderUsingTransport();

        mailSender.send("test", "nothing", TO, null, null);

        verify(transport).connect(eq("smtp.file.example"), isNull(), isNull());
        assertEquals("file-sender@example.com", senderOfSentMessage());
    }

    @Test
    public void shouldFallBackToGlobalPropertyForSenderWhenMailConfigFileHasNone() throws Exception {
        writeMailConfigFile("mail.send=true", "mail.smtp.host=smtp.file.example");
        globalProperties.put("mail.from", "gp-sender@example.com");
        DefaultMailSender mailSender = mailSenderUsingTransport();

        mailSender.send("test", "nothing", TO, null, null);

        assertEquals("gp-sender@example.com", senderOfSentMessage());
    }

    @Test
    public void shouldGetTheTransportFromTheMailSession() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        Session session = Session.getInstance(properties);

        Transport defaultTransport = new DefaultMailSender(administrationService).getTransport(session);

        assertNotNull(defaultTransport);
    }

    private DefaultMailSender mailSenderUsingTransport() throws Exception {
        DefaultMailSender mailSender = Mockito.spy(new DefaultMailSender(administrationService));
        doReturn(transport).when(mailSender).getTransport(any(Session.class));
        return mailSender;
    }

    private void writeMailConfigFile(String... lines) throws IOException {
        Files.write(applicationDataDirectory.newFile("mail-config.properties").toPath(), Arrays.asList(lines));
    }

    private String senderOfSentMessage() throws MessagingException {
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(transport).sendMessage(sent.capture(), any(Address[].class));
        return ((InternetAddress) sent.getValue().getFrom()[0]).getAddress();
    }

}
