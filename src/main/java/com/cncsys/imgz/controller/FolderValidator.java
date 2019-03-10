package com.cncsys.imgz.controller;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ArrayUtils;

import com.cncsys.imgz.model.FolderForm;
import com.cncsys.imgz.model.FolderForm.Share;
import com.cncsys.imgz.model.FolderForm.Upload;
import com.cncsys.imgz.model.LoginUser;

@Component
public class FolderValidator implements SmartValidator {

	@Value("${upload.file.size}")
	private long UPLOAD_SIZE;

	@Value("${folder.expired.days}")
	private int EXPIRED_DAYS;

	@Override
	public boolean supports(Class<?> clazz) {
		return FolderForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, new Object[] {});
	}

	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		FolderForm form = (FolderForm) target;

		if (ArrayUtils.contains(validationHints, Upload.class)) {
			long size = 0L;
			for (MultipartFile file : form.getFiles()) {
				if (file.isEmpty()) {
					errors.rejectValue("files", "validation.upload.nofile");
					return;
				}

				size += file.getSize();
				if (!file.getOriginalFilename().toLowerCase().matches(".+(\\.zip|\\.jpg|\\.jpeg|\\.mp4)$")) {
					errors.rejectValue("files", "validation.upload.invalid");
					return;
				}
			}

			if (size > UPLOAD_SIZE) {
				errors.rejectValue("files", "validation.upload.invalid");
			}
		} else if (ArrayUtils.contains(validationHints, Share.class)) {
			String password = form.getPassword();
			if (password == null || password.isEmpty() || password.length() < 4 || password.length() > 16
					|| !password.matches("^[a-zA-Z0-9]+$")) {
				errors.rejectValue("password", "validation.share.password");
			}

			LocalDate plansdt = form.getPlansdt();
			if (!errors.hasFieldErrors("plansdt")) {
				if (plansdt == null || plansdt.getYear() < 1970) {
					errors.rejectValue("plansdt", "typeMismatch.org.joda.time.LocalDate");
					return;
				}

				LocalDate mindt = LocalDate.now();
				LocalDate maxdt = LocalDate.now().plusDays(EXPIRED_DAYS / 2);
				if (mindt.isAfter(plansdt) || maxdt.isBefore(plansdt)) {
					errors.rejectValue("plansdt", "validation.share.plansdt",
							new Object[] { EXPIRED_DAYS / 2 }, null);
				}
			}

			LocalDate expiredt = form.getExpiredt();
			if (!errors.hasFieldErrors("expiredt")) {
				if (expiredt == null || expiredt.getYear() < 1970) {
					errors.rejectValue("expiredt", "typeMismatch.org.joda.time.LocalDate");
					return;
				}

				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				LoginUser user = (LoginUser) auth.getPrincipal();
				if (user.isVip()) {
					LocalDate mindt = plansdt;
					if (mindt.isAfter(expiredt)) {
						errors.rejectValue("expiredt", "validation.vipshare.expiredt");
					}
				} else {
					LocalDate mindt = plansdt;
					LocalDate maxdt = plansdt.plusDays(EXPIRED_DAYS);
					if (mindt.isAfter(expiredt) || maxdt.isBefore(expiredt)) {
						errors.rejectValue("expiredt", "validation.share.expiredt",
								new Object[] { EXPIRED_DAYS }, null);
					}
				}
			}
		}
	}
}
