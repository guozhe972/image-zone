package com.cncsys.imgz.helper;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

	public String sendRegisterConfirm(String to) {
		Locale locale = LocaleContextHolder.getLocale();

		String code = String.format("%06d", intRandom.nextInt(1000000));
		String subject = messageSource.getMessage("email.register.subject", null, locale);
		String text = messageSource.getMessage("email.register.message", new String[] { code }, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		mailSender.send(mail);

		return code;
	}

	public void sendMailConfirm(String to) {
		Locale locale = LocaleContextHolder.getLocale();

		String subject = messageSource.getMessage("email.confirm.subject", null, locale);
		String text = messageSource.getMessage("email.confirm.message", null, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		mailSender.send(mail);
	}

	public void sendOrderDone(String to, List<String> param) {
		Locale locale = LocaleContextHolder.getLocale();

		String order = param.get(0);
		String expiry = param.get(1);
		String link = param.get(2);
		//String price = param.get(3);
		//String amount = param.get(4);

		String subject = messageSource.getMessage("email.order.subject", null, locale);
		String text = messageSource.getMessage("email.order.message", new String[] { order, expiry, link }, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		mailSender.send(mail);
	}
}
