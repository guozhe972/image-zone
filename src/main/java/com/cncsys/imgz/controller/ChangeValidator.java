package com.cncsys.imgz.controller;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.thymeleaf.util.ArrayUtils;

import com.cncsys.imgz.model.ChangeForm;
import com.cncsys.imgz.model.ChangeForm.Input;

@Component
public class ChangeValidator implements SmartValidator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ChangeForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, new Object[] {});
	}

	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		ChangeForm form = (ChangeForm) target;

		if (ArrayUtils.contains(validationHints, Input.class)) {
			String password = form.getPassword();
			String confirm = form.getConfirm();

			if (password == null || password.isEmpty() || password.length() < 8 || password.length() > 16
					|| !password.matches("^[\\u0020-\\u007E]+$") || !password.matches(".*[a-zA-Z]+.*")
					|| !password.matches(".*[0-9]+.*")) {
				errors.rejectValue("password", "validation.change.password");
			} else {
				if (!password.equals(confirm)) {
					errors.rejectValue("confirm", "validation.change.confirm");
				}
			}
		}
	}
}
