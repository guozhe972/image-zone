package com.cncsys.imgz.controller;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

import com.cncsys.imgz.model.BankForm;

@Component
public class BankValidator implements SmartValidator {

	@Override
	public boolean supports(Class<?> clazz) {
		return BankForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, new Object[] {});
	}

	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		BankForm form = (BankForm) target;


	}
}
