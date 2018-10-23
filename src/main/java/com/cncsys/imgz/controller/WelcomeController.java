package com.cncsys.imgz.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cncsys.imgz.helper.CodeParser;

@Controller
public class WelcomeController {
	//private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

	@Autowired
	private CodeParser codeParser;

	@GetMapping("/")
	public String welcome(Locale locale) {
		return "/index_" + locale.getLanguage().toLowerCase();
	}

	@GetMapping("/login/{usernm}/{folder}/{token}")
	public String login(@PathVariable("usernm") String usernm, @PathVariable("folder") String folder,
			@PathVariable("token") String token, Model model) {

		String passwd = codeParser.queryDecrypt(token);
		if (passwd == null) {
			return "/system/none";
		}

		String username = usernm;
		if (!"00".equals(folder)) {
			username = usernm + "." + folder;
		}

		model.addAttribute("username", username);
		model.addAttribute("password", passwd);
		return "/auth/login";
	}

	@GetMapping("/about/site")
	public String aboutSite(Locale locale) {
		return "/about/about_" + locale.getLanguage().toLowerCase();
	}

	@GetMapping("/about/terms")
	public String siteTerms(Locale locale) {
		return "/about/terms_" + locale.getLanguage().toLowerCase();
	}

	@GetMapping("/about/privacy")
	public String privacyPolicy(Locale locale) {
		return "/about/privacy_" + locale.getLanguage().toLowerCase();
	}
}
