package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.notification.MailSender;

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
import java.util.Date;
import java.util.Properties;

public class DefaultMailSender implements MailSender {
    private Log log = LogFactory.getLog(this.getClass());
    private Session session = null;

    @Override
    public void send(String subject, String bodyText, String[] to, String[] cc, String[] bcc) {
        try {
            MimeMessage mail = new MimeMessage(getSession());
            mail.setFrom(new InternetAddress(Context.getAdministrationService().getGlobalProperty("mail.from", "Bahmni")));
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
            //mail.setContent(bodyText, "text/html;charset=utf-8");
            mimeBodyPart.setContent(bodyText, "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            mail.setContent(multipart);
            Transport.send(mail);
        }
        catch (Exception e) {
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

    public Session getSession() {
        if (session == null) {
            AdministrationService as = Context.getAdministrationService();
            Properties p = new Properties();
            p.put("mail.transport.protocol", as.getGlobalProperty("mail.transport_protocol", "smtp"));
            p.put("mail.smtp.host", as.getGlobalProperty("mail.smtp_host", "localhost"));
            p.put("mail.smtp.port", as.getGlobalProperty("mail.smtp_port", "25")); // mail.smtp_port
            p.put("mail.smtp.auth", as.getGlobalProperty("mail.smtp_auth", "false")); // mail.smtp_auth
            p.put("mail.smtp.starttls.enable", as.getGlobalProperty("mail.smtp.starttls.enable", "false"));
            p.put("mail.debug", as.getGlobalProperty("mail.debug", "false"));
            p.put("mail.from", as.getGlobalProperty("mail.from", ""));

            final String user = as.getGlobalProperty("mail.user", "");
            final String password = as.getGlobalProperty("mail.password", "");

            if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
                session = Session.getInstance(p, new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
            }
            else {
                session = Session.getInstance(p);
            }
        }
        return session;
    }
}
