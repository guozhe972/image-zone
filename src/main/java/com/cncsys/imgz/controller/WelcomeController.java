package com.cncsys.imgz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

	@GetMapping("/")
	public String welcome() {
		return "/index";
	}

	@GetMapping("/about/site")
	public String aboutSite() {
		return "/about/about";
	}

	@GetMapping("/about/terms")
	public String siteTerms() {
		return "/about/terms";
	}

	@GetMapping("/about/privacy")
	public String privacyPolicy() {
		return "/about/privacy";
	}
}
