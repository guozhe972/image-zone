package com.cncsys.imgz.controller;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.model.SignupForm;
import com.cncsys.imgz.model.SignupForm.Confirm;
import com.cncsys.imgz.model.SignupForm.Input;
import com.cncsys.imgz.service.AccountService;

@Controller
@RequestMapping("/signup")
@SessionAttributes("signupForm")
public class SignupController {

	private static final String FORM_MODEL_KEY = "signupForm";

	@Autowired
	private AccountService accountService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private SignupValidator signupValidator;

	@ModelAttribute(FORM_MODEL_KEY)
	public SignupForm initSignupForm() {
		SignupForm form = new SignupForm();
		return form;
	}

	@InitBinder(FORM_MODEL_KEY)
	public void initSignupBinder(WebDataBinder binder) {
		binder.addValidators(signupValidator);
	}

	@GetMapping("/input")
	public String input(Model model) {
		if (!model.containsAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY)) {
			model.addAttribute(FORM_MODEL_KEY, initSignupForm());
		}
		return "/auth/signup";
	}

	//	@PostMapping(path = "/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	//	public @ResponseBody SignupForm check(@RequestBody Map<String, Object> json) {

	@PostMapping(path = "/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String check(@RequestBody Map<String, Object> json) {
		return String.valueOf(accountService.isExistUser(json.get("username").toString()));
	}

	@PostMapping("/mail")
	public String mail(@Validated(Input.class) SignupForm form, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY, result);
			return "redirect:/signup/input";
		}

		String code = mailHelper.sendRegisterConfirm(form.getEmail());
		form.setToken(passwordEncoder.encode(code));
		form.setCode(null);

		return "redirect:/signup/confirm";
	}

	@GetMapping("/confirm")
	public String confirm(Model model) {
		SignupForm form = (SignupForm) model.asMap().get(FORM_MODEL_KEY);
		String token = form.getToken();
		if (token == null || token.isEmpty()) {
			return "redirect:/signup/input";
		}
		return "/auth/confirm";
	}

	@PostMapping("/register")
	public String register(@Validated(Confirm.class) SignupForm form, BindingResult result,
			RedirectAttributes redirectAttributes, SessionStatus sessionStatus, HttpServletRequest request)
			throws ServletException {

		if (result.hasErrors()) {
			form.setToken(null);
			form.setCode(null);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY, result);
			return "redirect:/signup/input";
		}

		accountService.registerUser(form.getUsername(), form.getPassword(), form.getEmail());
		request.login(form.getUsername(), form.getPassword());
		accountService.updateLogindt(form.getUsername());
		sessionStatus.setComplete();

		return "redirect:/auth/signin";
	}
}
