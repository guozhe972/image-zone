package com.cncsys.imgz.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
@RequestMapping("/system")
public class SystemController {

	@PostMapping("/lang")
	public String changeLang(HttpServletRequest request, HttpServletResponse response) {
		String lang = request.getParameter("lang");
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		localeResolver.setLocale(request, response, new Locale(lang));
		return "redirect:" + request.getHeader("Referer");
	}

	@GetMapping("/none")
	public String notFound() {
		return "/system/none";
	}

	@GetMapping("/error")
	public String serverError() {
		return "/system/error";
	}

	@GetMapping("/denied")
	public String accessDenied() {
		return "/system/denied";
	}

	@GetMapping("/about")
	public String aboutSite() {
		return "/system/about";
	}

	@GetMapping("/terms")
	public String siteTerms() {
		return "/system/terms";
	}

	@GetMapping("/privacy")
	public String privacyPolicy() {
		return "/system/privacy";
	}

}
