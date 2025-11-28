package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.util.OpenmrsUtil;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class DefaultMailSender implements MailSender {
    private static final String EMAIL_PROPERTIES_FILENAME = "mail-config.properties";
    private Log log = LogFactory.getLog(this.getClass());
    private volatile Session session = null;

    private AdministrationService administrationService;

    public DefaultMailSender(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public void send(String subject, String bodyText, String[] to, String[] cc, String[] bcc) {
        try {
            MimeMessage mail = new MimeMessage(getSession());
            mail.setFrom(new InternetAddress(this.administrationService.getGlobalProperty("mail.from", "")));
            Address[] toAddresses = new Address[1];
            toAddresses[0] = new InternetAddress(to[0]);
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

            Transport transport = null;
            try {
                transport = session.getTransport();
                log.info("Sending Mail");
                if (!transport.isConnected()) {
                    transport.connect(
                            administrationService.getGlobalProperty("mail.smtp_host"),
                            Integer.parseInt(administrationService.getGlobalProperty("mail.smtp_port")),
                            administrationService.getGlobalProperty("mail.user"),
                            administrationService.getGlobalProperty("mail.password")
                    );
                }
                transport.sendMessage(mail, mail.getAllRecipients());
                log.info("Mail Sent");
            } finally {
                if (transport != null && transport.isConnected()) {
                    transport.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while sending email", e);
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
                    } else {
                        administrationService.setGlobalProperty("mail.transport_protocol", sessionProperties.getProperty("mail.transport.protocol"));
                        administrationService.setGlobalProperty("mail.smtp_host", sessionProperties.getProperty("mail.smtp.host"));
                        administrationService.setGlobalProperty("mail.smtp_port", sessionProperties.getProperty("mail.smtp.port"));
                        administrationService.setGlobalProperty("mail.smtp_auth", sessionProperties.getProperty("mail.smtp.auth"));
                        administrationService.setGlobalProperty("mail.smtp.starttls.enable", sessionProperties.getProperty("mail.smtp.starttls.enable"));
                        administrationService.setGlobalProperty("mail.smtp.ssl.enable", sessionProperties.getProperty("mail.smtp.ssl.enable"));
                        administrationService.setGlobalProperty("mail.debug", sessionProperties.getProperty("mail.debug"));
                        administrationService.setGlobalProperty("mail.from", sessionProperties.getProperty("mail.from"));
                        administrationService.setGlobalProperty("mail.user", sessionProperties.getProperty("mail.user"));
                        administrationService.setGlobalProperty("mail.password", sessionProperties.getProperty("mail.password"));

                        Properties p = new Properties();
                        String[] propertyKeys = {"mail.from", "mail.user", "mail.password", "mail.transport.protocol", "mail.smtp.host", "mail.smtp.port", "mail.smtp.auth", "mail.smtp.starttls.enable", "mail.smtp.ssl.enable", "mail.debug"};

                        for (String key : propertyKeys) {
                            String value = sessionProperties.getProperty(key);
                            p.put(key, (value != null) ? value : "");
                        }

                        sessionProperties = p;
                        session = Session.getInstance(sessionProperties);
                    }
                    final String user = sessionProperties.getProperty("mail.user");
                    final String password = sessionProperties.getProperty("mail.password");
                    if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
                        session = Session.getInstance(sessionProperties, new Authenticator() {
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(user, password);
                            }
                        });
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
        Properties p = new Properties();
        p.put("mail.transport.protocol", administrationService.getGlobalProperty("mail.transport_protocol", "smtp"));
        p.put("mail.smtp.host", administrationService.getGlobalProperty("mail.smtp_host", ""));
        p.put("mail.smtp.port", administrationService.getGlobalProperty("mail.smtp_port", "25")); // mail.smtp_port
        p.put("mail.smtp.auth", administrationService.getGlobalProperty("mail.smtp_auth", "false")); // mail.smtp_auth
        p.put("mail.smtp.starttls.enable", administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true"));
        p.put("mail.smtp.ssl.enable", administrationService.getGlobalProperty("mail.smtp.ssl.enable", "true"));
        p.put("mail.debug", administrationService.getGlobalProperty("mail.debug", "false"));
        p.put("mail.from", administrationService.getGlobalProperty("mail.from", ""));
        p.put("mail.user", administrationService.getGlobalProperty("mail.user", ""));
        p.put("mail.password", administrationService.getGlobalProperty("mail.password", ""));
        //p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return p;
    }

    private Properties mailSessionPropertiesFromPath() {
        Path propertyFilePath = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), EMAIL_PROPERTIES_FILENAME);
        if (Files.exists(propertyFilePath)) {
            Properties properties = new Properties();
            try {
                log.info("Reading properties from: " + propertyFilePath);
                properties.load(Files.newInputStream(propertyFilePath));
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
