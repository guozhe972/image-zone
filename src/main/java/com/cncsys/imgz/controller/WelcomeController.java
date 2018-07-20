package com.cncsys.imgz.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

	@GetMapping("/")
	public String welcome(Locale locale) {
		//String lang = LocaleContextHolder.getLocale().getLanguage();
		//String lang = locale.getLanguage();
		return "/index";
	}

	@GetMapping("/auth/signin")
	public String signin() {
		return "/auth/signin";
	}

	//@GetMapping("/hello")
	//public String hello(@RequestParam(name = "name", required = false) String name) {
	//@GetMapping("/hello/{age}")
	//public String hello(@PathVariable("age") Integer age) {
	//public String hello(@SessionAttribute("age") Integer age) {
	//public String hello(@SessionAttribute(name = "age", required = false) Integer age) {

}
