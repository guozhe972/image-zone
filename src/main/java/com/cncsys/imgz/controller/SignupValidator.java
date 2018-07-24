package com.cncsys.imgz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.thymeleaf.util.ArrayUtils;

import com.cncsys.imgz.model.SignupForm;
import com.cncsys.imgz.model.SignupForm.Confirm;
import com.cncsys.imgz.model.SignupForm.Input;
import com.cncsys.imgz.service.AccountService;

@Component
public class SignupValidator implements SmartValidator {

	@Autowired
	private AccountService accountService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public boolean supports(Class<?> clazz) {
		return SignupForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, new Object[] {});
	}

	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		SignupForm form = (SignupForm) target;

		if (ArrayUtils.contains(validationHints, Input.class)) {
			String username = form.getUsername();
			String email = form.getEmail();
			String password = form.getPassword();
			String confirm = form.getConfirm();

			if (username == null || username.isEmpty() || username.length() < 4 || username.length() > 16
					|| !username.matches("^[a-zA-Z0-9]+[a-zA-Z0-9_\\-]*$")) {
				errors.rejectValue("username", "validation.signup.username");
			} else if (accountService.isExistUser(username)) {
				errors.rejectValue("username", "validation.signup.existuser");
			}

			if (email == null || email.isEmpty() || !email.matches(
					"^(([0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*)|(\"[^\"]*\"))@[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*$")) {
				errors.rejectValue("email", "validation.signup.email");
			}

			if (password == null || password.isEmpty() || password.length() < 4 || password.length() > 16
					|| !password.matches("^[\\u0020-\\u007E]+$") || !password.matches(".*[a-zA-Z]+.*")
					|| !password.matches(".*[0-9]+.*")) {
				errors.rejectValue("password", "validation.signup.password");
			} else {
				if (!password.equals(confirm)) {
					errors.rejectValue("confirm", "validation.signup.confirm");
				}
			}
		} else if (ArrayUtils.contains(validationHints, Confirm.class)) {
			String code = form.getCode();
			String token = form.getToken();

			if (code == null || code.isEmpty() || token == null || token.isEmpty()
					|| !passwordEncoder.matches(code, token)) {
				errors.rejectValue("code", "validation.signup.code");
			}
		}
	}
}
