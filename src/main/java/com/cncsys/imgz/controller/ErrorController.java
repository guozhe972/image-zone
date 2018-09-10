package com.cncsys.imgz.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorController {
	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String exception(final Throwable throwable, final Model model, HttpServletRequest request) {
		String ipAddr = request.getHeader("X-Forwarded-For");
		if (ipAddr == null) {
			ipAddr = request.getRemoteAddr();
		}
		logger.error("Exception - request from " + ipAddr, throwable);
		String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");
		model.addAttribute("errorMessage", errorMessage);
		return "/system/error";
	}
}
