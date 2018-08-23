package com.cncsys.imgz.controller;

import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.AccountEntity.Authority;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.model.ChangeForm;
import com.cncsys.imgz.model.ChangeForm.Input;
import com.cncsys.imgz.service.AccountService;

@Controller
@RequestMapping("/auth")
@SessionAttributes({ "usernm", "token" })
public class AuthController {

	@Autowired
	private CodeParser codeParser;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private AccountService accountService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ChangeValidator changeValidator;

	@InitBinder("changeForm")
	public void initChangeBinder(WebDataBinder binder) {
		binder.addValidators(changeValidator);
	}

	@GetMapping("/signin")
	public String signin() {
		return "/auth/signin";
	}

	@PostMapping("/forgot")
	public ResponseEntity<String> forgot(@RequestParam("username") String username, UriComponentsBuilder builder,
			Locale locale) {
		if (username == null || username.isEmpty()) {
			return ResponseEntity.badRequest().cacheControl(CacheControl.noCache()).body("error");
		}

		AccountEntity account = accountService.getAccountInfo(username);
		if (account != null) {
			if (account.getAuthority() == Authority.USER && account.isEnabled()) {
				String[] param = new String[1];
				String link = "/auth/reset/" + account.getUsername() + "/" + codeParser.encrypt(account.getPassword());
				param[0] = builder.path(link).build().toUriString();
				mailHelper.sendForgotPass(account.getEmail(), param);
			}
		}
		return ResponseEntity.ok()
				.body(messageSource.getMessage("info.forgot.send", new Object[] { username }, locale));
	}

	@GetMapping("/reset/{usernm}/{token}")
	public String reset(@PathVariable("usernm") String usernm, @PathVariable("token") String token, Model model) {
		String passwd = codeParser.decrypt(token);
		if (passwd == null) {
			return "/system/none";
		}

		AccountEntity account = accountService.getForgotAccount(usernm, passwd);
		if (account == null) {
			return "/system/none";
		} else {
			if (!model.containsAttribute("changeForm")) {
				ChangeForm form = new ChangeForm();
				form.setUsername(usernm);
				model.addAttribute("changeForm", form);
			}
		}
		model.addAttribute("usernm", usernm);
		model.addAttribute("token", token);
		return "/auth/reset";
	}

	@PostMapping("/change")
	public String change(@SessionAttribute("usernm") String usernm, @SessionAttribute("token") String token,
			@ModelAttribute @Validated(Input.class) ChangeForm form, BindingResult result,
			RedirectAttributes redirectAttributes, SessionStatus sessionStatus, HttpServletRequest request)
			throws ServletException {

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("changeForm", form);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "changeForm", result);
			return "redirect:/auth/reset/" + usernm + "/" + token;
		}

		if (usernm == null || usernm.isEmpty() || !usernm.equals(form.getUsername())) {
			return "redirect:/system/error";
		}

		String email = accountService.changePassword(form.getUsername(), form.getPassword());
		request.login(form.getUsername(), form.getPassword());
		accountService.updateLogindt(form.getUsername());

		String[] param = new String[2];
		param[0] = form.getUsername();
		param[1] = form.getPassword();
		mailHelper.sendChangeSuccess(email, param);
		sessionStatus.setComplete();
		return "redirect:/auth/signin";
	}
}
