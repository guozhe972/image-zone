package com.cncsys.imgz.controller;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.thymeleaf.util.ArrayUtils;

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

		String bank = form.getBank().trim();
		if (bank == null || bank.isEmpty()) {
			errors.rejectValue("bank", "validation.bank.bank");
		}

		String branch = form.getBranch().trim();
		if (branch == null || branch.isEmpty()) {
			errors.rejectValue("branch", "validation.bank.branch");
		}

		int actype = form.getActype();
		if (!ArrayUtils.contains(BankForm.AcTypes, actype)) {
			errors.rejectValue("actype", "validation.bank.actype");
		}

		String acnumber = form.getAcnumber().trim();
		if (acnumber == null || acnumber.isEmpty()) {
			errors.rejectValue("acnumber", "validation.bank.acnumber");
		}

		String acname = form.getAcname().trim();
		if (acname == null || acname.isEmpty()) {
			errors.rejectValue("acname", "validation.bank.acname");
		}
	}
}
