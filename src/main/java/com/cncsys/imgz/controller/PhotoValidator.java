package com.cncsys.imgz.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.thymeleaf.util.ArrayUtils;

import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.model.PhotoForm.Price;

@Component
public class PhotoValidator implements SmartValidator {

	@Value("${photo.price.min}")
	private int PRICE_MIN;

	@Value("${photo.price.max}")
	private int PRICE_MAX;

	@Override
	public boolean supports(Class<?> clazz) {
		return PhotoForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, new Object[] {});
	}

	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		PhotoForm form = (PhotoForm) target;

		if (ArrayUtils.contains(validationHints, Price.class)) {
			int price = form.getPrice();
			if (price < PRICE_MIN || price > PRICE_MAX) {
				errors.rejectValue("price", "validation.photo.price",
						new Object[] { String.format("%,d", PRICE_MIN), String.format("%,d", PRICE_MAX) }, null);
				return;
			}
		}
	}
}
