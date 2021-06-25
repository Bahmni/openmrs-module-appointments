package org.openmrs.module.appointments.notification.impl;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.mail.MailMessageSender;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Date;
import java.util.Properties;

public class DefaultMessageSender implements MailSender {

    private MailMessageSender mailMessageSender;
    private Session mailSession;

    @Override
    public void send(String subject, String body, String[] to, String[] cc, String[] bcc) {
        initializeMessageSender();
        Message mail = new Message();
        mail.setId(20);
        mail.setRecipients(to[0]);
        //mail.setSender(Context.getAdministrationService().getGlobalProperty("mail.user"));
        mail.setContentType(Context.getAdministrationService().getGlobalProperty("mail.default_content_type"));
        mail.setSubject(subject);
        mail.setContent(body);
        mail.setSentDate(new Date());
        try {
            //Context.getMessageService().sendMessage(mail);
            mailMessageSender.send(mail);
        } catch (MessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private javax.mail.Session getMailSession() {
        if (mailSession == null) {
            AdministrationService adminService = Context.getAdministrationService();
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", adminService.getGlobalProperty("mail.transport_protocol"));
            props.setProperty("mail.smtp.host", adminService.getGlobalProperty("mail.smtp_host"));
            props.setProperty("mail.smtp.port", adminService.getGlobalProperty("mail.smtp_port"));
            props.setProperty("mail.from", adminService.getGlobalProperty("mail.from"));
            props.setProperty("mail.debug", adminService.getGlobalProperty("mail.debug"));
            props.setProperty("mail.smtp.auth", adminService.getGlobalProperty("mail.smtp_auth"));

            Authenticator auth = new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(adminService.getGlobalProperty("mail.user"),
                            adminService.getGlobalProperty("mail.password"));
                }
            };
            mailSession = Session.getInstance(props, auth);
        }
        return mailSession;
    }

    private void initializeMessageSender() {
        if (mailMessageSender == null) {
            synchronized (MailMessageSender.class) {
                if (mailMessageSender == null) {
                    mailMessageSender = new MailMessageSender(getMailSession());
                }
            }
        }
    }
}
