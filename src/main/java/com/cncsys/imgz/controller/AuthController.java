package com.cncsys.imgz.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.AccountEntity.Authority;
import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.service.AccountService;

@Controller
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private AccountService accountService;

	@GetMapping("/signin")
	public String signin() {
		return "/auth/signin";
	}

	@GetMapping("/reset/{usernm}/{passwd}")
	public String reset(@PathVariable("usernm") String usernm, @PathVariable("passwd") String passwd, Model model) {
		AccountEntity account = accountService.getForgotAccount(usernm, passwd);
		if (account != null) {
			model.addAttribute("usernm", usernm);
		} else {
			return "/system/none";
		}
		return "/auth/reset";
	}

	@PostMapping(path = "/forgot", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String forgot(@RequestBody Map<String, Object> json, UriComponentsBuilder builder) {
		String username = json.get("usernm").toString();
		AccountEntity account = accountService.getAccountInfo(username);
		if (account != null) {
			if (account.getAuthority() == Authority.USER) {
				String[] param = new String[1];
				String link = "/auth/reset/" + account.getUsername() + "/" + account.getPassword();
				param[0] = builder.path(link).build().toUriString();
				mailHelper.sendForgotPass(account.getEmail(), param);
				return "OK";
			} else {
				return "NG";
			}
		}
		return "ERR";
	}

}
