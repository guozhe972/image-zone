package com.cncsys.imgz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cncsys.imgz.helper.CodeParser;

@Controller
public class DownloadController {
	private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

	@Autowired
	private CodeParser codeParser;

	@GetMapping("/download/{order}/{token}")
	public String home(@PathVariable("order") String order, @PathVariable("token") String token, Model model) {
		logger.info("order: " + order);
		logger.info("token: " + token);
		String email = codeParser.decrypt(token);
		if (email == null) {
			return "/system/none";
		}

		logger.info("email: " + email);

		return "/system/download";
	}
}
