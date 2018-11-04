package com.cncsys.imgz.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
@RequestMapping("/auth")
@SessionAttributes("signupForm")
public class SignupController {

	private static final String FORM_MODEL_KEY = "signupForm";

	@Value("${email.send}")
	private boolean MAIL_SEND;

	@Autowired
	private AccountService accountService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private MessageSource messageSource;

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

	@GetMapping("/signup")
	public String signup(Model model) {
		if (!model.containsAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY)) {
			model.addAttribute(FORM_MODEL_KEY, initSignupForm());
		}
		return "/auth/signup";
	}

	@PostMapping(path = "/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String check(@RequestBody Map<String, Object> json) {
		return String.valueOf(accountService.isExistUser(json.get("username").toString()));
	}

	@PostMapping("/send")
	public String send(@Validated(Input.class) SignupForm form, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY, result);
			return "redirect:/auth/signup";
		}

		String code = mailHelper.sendRegisterConfirm(form.getEmail());
		form.setToken(passwordEncoder.encode(code));
		form.setCode(code);
		if (MAIL_SEND)
			form.setCode(null);

		return "redirect:/auth/confirm";
	}

	@GetMapping("/confirm")
	public String confirm(Model model) {
		SignupForm form = (SignupForm) model.asMap().get(FORM_MODEL_KEY);
		String token = form.getToken();
		if (token == null || token.isEmpty()) {
			return "redirect:/auth/signup";
		}
		return "/auth/confirm";
	}

	@PostMapping("/register")
	public String register(@Validated(Confirm.class) SignupForm form, BindingResult result,
			RedirectAttributes redirectAttributes, Locale locale, SessionStatus sessionStatus,
			HttpServletRequest request)
			throws ServletException {

		if (result.hasErrors()) {
			form.setToken(null);
			form.setCode(null);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY, result);
			if (result.hasFieldErrors("code")) {
				List<String> errors = new ArrayList<String>();
				errors.add(messageSource.getMessage(result.getFieldError("code").getCode(), null, locale));
				redirectAttributes.addFlashAttribute("errors", errors);
			}
			return "redirect:/auth/signup";
		}

		accountService.registerUser(form.getUsername(), form.getPassword(), form.getEmail());
		request.login(form.getUsername(), form.getPassword());
		accountService.updateLogindt(form.getUsername());

		String[] param = new String[2];
		param[0] = form.getUsername();
		param[1] = form.getPassword();
		mailHelper.sendRegisterSuccess(form.getEmail(), param);
		sessionStatus.setComplete();
		return "redirect:/auth/signin";
	}
}
