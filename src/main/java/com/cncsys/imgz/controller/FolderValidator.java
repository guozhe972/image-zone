package com.cncsys.imgz.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ArrayUtils;

import com.cncsys.imgz.model.FolderForm;
import com.cncsys.imgz.model.FolderForm.Upload;

@Component
public class FolderValidator implements SmartValidator {

	@Value("${upload.file.size}")
	private long UPLOAD_SIZE;

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
					errors.rejectValue("files", "message.upload.error");
					return;
				}

				size += file.getSize();
				if (!file.getOriginalFilename().toLowerCase().matches(".+(\\.zip|\\.jpg|\\.jpeg)$")) {
					errors.rejectValue("files", "message.upload.error");
					return;
				}
			}

			if (size > UPLOAD_SIZE) {
				errors.rejectValue("files", "message.upload.error");
			}
		}
	}
}
