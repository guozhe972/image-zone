package com.cncsys.imgz.helper;

import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class MailHelper {

	@Value("${email.from}")
	private String MAIL_FROM;

	@Autowired
	private MailSender mailSender;

	@Autowired
	private MessageSource messageSource;

	private final Random intRandom = new Random();

	public String sendRegisterMail(String to, Locale locale) {

		String code = String.format("%06d", intRandom.nextInt(1000000));
		String subject = messageSource.getMessage("email.register.subject", null, locale);
		String text = messageSource.getMessage("email.register.message", new String[] { code }, locale);

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(MAIL_FROM);
		msg.setTo(to);
		msg.setSubject(subject);
		msg.setText(text);
		mailSender.send(msg);

		return code;
	}
}
