package com.empOnboarding.api.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.EmailDetailsDTO;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class MailerService {
	private final JavaMailSender javaMailSender;

	@Value("${environment}")
	private String env;

	@Value("${exception.mail.address}")
	private String emails;

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private String port;

	@Value("${spring.mail.username}")
	private String fromAddress;

	@Value("${spring.mail.password}")
	private String password;

	public MailerService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void sendEmailOnException(Exception exception) {
		EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO();
		try {
			StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			emailDetailsDTO.setToAddress(emails.split(","));
			emailDetailsDTO.setSubject(Constants.EXCEPTION_MAIL_SUBJECT.getValue());
			emailDetailsDTO.setEmailBody(
					Constants.ENVIRONMENT.getValue() + env + Constants.EXCEPTION.getValue() + sw);
			sendHTMLMail(emailDetailsDTO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void sendMail(EmailDetailsDTO emailDetailsDTO) {
        if (!CommonUtls.isEmpty(emailDetailsDTO)) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setText(
                    !CommonUtls.isEmpty(emailDetailsDTO.getEmailBody()) ? emailDetailsDTO.getEmailBody() : "");
            message.setSubject(
                    !CommonUtls.isEmpty(emailDetailsDTO.getSubject()) ? emailDetailsDTO.getSubject() : "");
            if (!CommonUtls.isEmpty(emailDetailsDTO.getToAddress())) {
                message.setTo(emailDetailsDTO.getToAddress());
                javaMailSender.send(message);
            }
        }
    }

	public synchronized void sendHTMLMail(EmailDetailsDTO emailDetailsDTO) throws Exception {
        if (!CommonUtls.isEmpty(emailDetailsDTO)) {
            Properties props = new Properties();
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.user", fromAddress);
            props.put("mail.smtp.password", password);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", true);
            Session session = Session.getInstance(props);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setContent(
                    !CommonUtls.isEmpty(emailDetailsDTO.getEmailBody()) ? emailDetailsDTO.getEmailBody() : "",
                    "text/html");
            if (!CommonUtls.isEmpty(emailDetailsDTO.getToAddress())) {
                String[] email = emailDetailsDTO.getToAddress();
                message.setRecipients(Message.RecipientType.TO, createAddress(email));
                if (!CommonUtls.isEmpty(emailDetailsDTO.getCCAddress())) {
                    String[] cclist = emailDetailsDTO.getCCAddress();
                    message.setRecipients(Message.RecipientType.CC, createAddress(cclist));
                }
            }
            message.setSubject(
                    !CommonUtls.isEmpty(emailDetailsDTO.getSubject()) ? emailDetailsDTO.getSubject() : "");
            Transport transport = session.getTransport("smtp");
            transport.connect(host, fromAddress, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
    }

	private Address[] createAddress(String[] cclist) throws AddressException {
		Address[] addresses = new Address[cclist.length];

		for (int i = 0; i < cclist.length; i++) {
            addresses[i] = new InternetAddress(cclist[i]);
        }
		return addresses;
	}

	public void sendEmailWithAttachment(String[] to, String subject, String text, String attachmentFileName,
			byte[] attachmentInputStream, boolean b) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        for (String mail : to) {
            helper.setTo(mail);
        }
        helper.setSubject(subject);
        helper.setText(text, true);
        if (!b) {
            helper.addAttachment(attachmentFileName, new ByteArrayResource(attachmentInputStream),
                    "application/pdf");
        }
        javaMailSender.send(mimeMessage);

    }

    public void sendTOTPEmail(String to, String totp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your One-Time Password (OTP)");
        message.setText("Your OTP is: " + totp);
        javaMailSender.send(message);
    }

}
