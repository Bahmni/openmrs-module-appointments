package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.module.appointments.util.GlobalPropertyLookupUtil;
import org.openmrs.util.OpenmrsUtil;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class DefaultMailSender implements MailSender {
    private static final String EMAIL_PROPERTIES_FILENAME = "mail-config.properties";
    private static final String MAIL_FROM = "mail.from";
    private static final String MAIL_SEND = "mail.send";
    private static final String MAIL_USER = "mail.user";
    private static final String MAIL_PASSWORD = "mail.password";
    private static final String FALSE = Boolean.FALSE.toString();
    private final Log log = LogFactory.getLog(this.getClass());
    private volatile Session session = null;

    private final AdministrationService administrationService;

    public DefaultMailSender(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public void send(String subject, String bodyText, String[] to, String[] cc, String[] bcc) {
        try {
            MimeMessage mail = new MimeMessage(getSession());
            if (!Objects.equals(mail.getSession().getProperty(MAIL_SEND), "true")) {
                log.info("Mail sending is disabled (" + MAIL_SEND + " is not 'true'). Skipping email: " + subject);
                return;
            }
            if (to == null || to.length == 0) {
                throw new IllegalArgumentException("At least one 'to' recipient is required to send an email");
            }
            mail.setFrom(new InternetAddress(resolveFromAddress(mail.getSession())));
            mail.setRecipients(Message.RecipientType.TO, getAddresses(to));
            if (cc != null && cc.length > 0) {
                mail.setRecipients(Message.RecipientType.CC, getAddresses(cc));
            }
            if (bcc != null && bcc.length > 0) {
                mail.setRecipients(Message.RecipientType.BCC, getAddresses(bcc));
            }
            mail.setSubject(subject);
            mail.setSentDate(new Date());

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            //TODO: might need to read from GP mail.default_content_type
            //mail.setContent(bodyText, "text/html;charset=utf-8");
            mimeBodyPart.setContent(bodyText, "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            mail.setContent(multipart);

            Session mailSession = mail.getSession();
            Transport transport = getTransport(mailSession);
            try {
                log.info("Sending Mail");
                transport.connect(mailSession.getProperty("mail.smtp.host"), mailSession.getProperty(MAIL_USER), mailSession.getProperty(MAIL_PASSWORD));
                transport.sendMessage(mail, mail.getAllRecipients());
                log.info("Mail Sent");
            } finally {
                closeQuietly(transport);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error occurred while sending email", e);
        }
    }

    // Package-private so tests can supply a Transport without needing a real SMTP server.
    Transport getTransport(Session mailSession) throws NoSuchProviderException {
        return mailSession.getTransport();
    }

    private String resolveFromAddress(Session mailSession) {
        String from = mailSession.getProperty(MAIL_FROM);
        if (StringUtils.isBlank(from)) {
            from = GlobalPropertyLookupUtil.withGlobalPropertyPrivilege(() ->
                    this.administrationService.getGlobalProperty(MAIL_FROM, ""));
        }
        if (StringUtils.isBlank(from)) {
            throw new IllegalStateException("Sender address is not configured. Set '" + MAIL_FROM
                    + "' in " + EMAIL_PROPERTIES_FILENAME + " or as a global property");
        }
        return from;
    }

    private void closeQuietly(Transport transport) {
        try {
            transport.close();
        } catch (MessagingException e) {
            log.warn("Could not close mail transport", e);
        }
    }

    private Address[] getAddresses(String[] addrs) throws AddressException {
        if (addrs != null && addrs.length > 0) {
            Address[] addresses = new Address[addrs.length];
            for (int i = 0; i < addrs.length; i++) {
                addresses[i] = new InternetAddress(addrs[i]);
            }
            return addresses;
        }
        return new Address[0];
    }

    private Session getSession() {
        if (session == null) {
            synchronized(this) {
                if (session == null) {
                    Properties sessionProperties = mailSessionPropertiesFromPath();
                    if (sessionProperties == null) {
                        log.info("Could not load mail properties from application data directory. Loading from OMRS settings.");
                        sessionProperties = mailSessionPropertiesFromOMRS();
                    }
                    final String user = sessionProperties.getProperty(MAIL_USER);
                    final String password = sessionProperties.getProperty(MAIL_PASSWORD);
                    if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
                        session = Session.getInstance(sessionProperties, new Authenticator() {
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(user, password);
                            }
                        });
                    }
                    else {
                        session = Session.getInstance(sessionProperties);
                    }
                }
            }
        }
        return session;
    }

    /**
     * To be used as fallback. Mail properties are visible in openmrs settings.
     * @param as
     * @return
     */
    private Properties mailSessionPropertiesFromOMRS() {
        return GlobalPropertyLookupUtil.withGlobalPropertyPrivilege(() -> {
            Properties p = new Properties();
            p.put("mail.transport.protocol", administrationService.getGlobalProperty("mail.transport_protocol", "smtp"));
            p.put("mail.smtp.host", administrationService.getGlobalProperty("mail.smtp_host", ""));
            p.put("mail.smtp.port", administrationService.getGlobalProperty("mail.smtp_port", "25")); // mail.smtp_port
            p.put("mail.smtp.auth", administrationService.getGlobalProperty("mail.smtp_auth", FALSE)); // mail.smtp_auth
            p.put("mail.smtp.starttls.enable", administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true"));
            p.put("mail.smtp.ssl.enable", administrationService.getGlobalProperty("mail.smtp.ssl.enable", "true"));
            p.put("mail.debug", administrationService.getGlobalProperty("mail.debug", FALSE));
            p.put(MAIL_FROM, administrationService.getGlobalProperty(MAIL_FROM, ""));
            p.put(MAIL_USER, administrationService.getGlobalProperty(MAIL_USER, ""));
            p.put(MAIL_PASSWORD, administrationService.getGlobalProperty(MAIL_PASSWORD, ""));
            // Disabled unless explicitly enabled, so the fallback never starts sending mail on its own.
            p.put(MAIL_SEND, administrationService.getGlobalProperty(MAIL_SEND, FALSE));
            //p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            return p;
        });
    }

    private Properties mailSessionPropertiesFromPath() {
        Path propertyFilePath = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), EMAIL_PROPERTIES_FILENAME);
        if (Files.exists(propertyFilePath)) {
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(propertyFilePath)) {
                log.info("Reading properties from: " + propertyFilePath);
                properties.load(in);
                return properties;
            } catch (IOException e) {
                log.error("Could not load email properties from: " + propertyFilePath, e);
            }
        } else {
            log.warn("No mail configuration defined at " + propertyFilePath);
        }
        return null;
    }

}
