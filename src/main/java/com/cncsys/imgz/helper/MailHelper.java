package com.cncsys.imgz.helper;

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

	@Value("${email.send}")
	private boolean MAIL_SEND;

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
		String subject = messageSource.getMessage("email.confirm.subject", null, locale);
		String text = messageSource.getMessage("email.confirm.message", new String[] { code }, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);

		return code;
	}

	public void sendRegisterSuccess(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.register.subject", null, locale);
		String text = messageSource.getMessage("email.register.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendVipUpgrade(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.upgrade.subject", null, locale);
		String text = messageSource.getMessage("email.upgrade.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendChangeSuccess(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.change.subject", null, locale);
		String text = messageSource.getMessage("email.change.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendShareFolder(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.shared.subject", null, locale);
		String text = messageSource.getMessage("email.shared.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendForgotPass(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.forgot.subject", null, locale);
		String text = messageSource.getMessage("email.forgot.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendMailReceive(String to) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.receive.subject", null, locale);
		String text = messageSource.getMessage("email.receive.message", null, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendOrderDone(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.order.subject", null, locale);
		String text = messageSource.getMessage("email.order.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}

	public void sendTransAccept(String to, String[] param) {
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("email.accept.subject", null, locale);
		String text = messageSource.getMessage("email.accept.message", param, locale);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(MAIL_FROM);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		if (MAIL_SEND)
			mailSender.send(mail);
	}
}
