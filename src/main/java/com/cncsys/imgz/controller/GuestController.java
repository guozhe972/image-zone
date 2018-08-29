package com.cncsys.imgz.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.PayForm;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.AccountService;
import com.cncsys.imgz.service.AsyncService;
import com.cncsys.imgz.service.OrderService;
import com.cncsys.imgz.service.PhotoService;

import co.omise.Client;
import co.omise.models.Charge;
import co.omise.models.ChargeStatus;
import co.omise.models.Transaction;

@Controller
@RequestMapping("/guest")
@SessionAttributes("cart")
public class GuestController {
	private static final Logger logger = LoggerFactory.getLogger(GuestController.class);
	private static final String FORM_MODEL_KEY = "cart";

	@ModelAttribute(FORM_MODEL_KEY)
	public List<PhotoForm> initCartList() {
		List<PhotoForm> cart = new ArrayList<PhotoForm>();
		return cart;
	}

	@Value("${order.download.limit}")
	private int DOWNLOAD_LIMIT;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Value("${order.file.path}")
	private String ORDER_PATH;

	@Value("${omise.secret.key}")
	private String OMISE_SKEY;

	@Autowired
	private PhotoService photoService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private CodeParser codeParser;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/home")
	public String home(Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		String[] guest = user.getUsername().split("\\.");
		model.addAttribute("username", guest[0]);
		model.addAttribute("folder", Integer.parseInt(guest[1]));

		List<PhotoForm> photos = new ArrayList<PhotoForm>();
		List<PhotoEntity> entity = photoService.getPhotosByGuest(user.getUsername());
		for (PhotoEntity photo : entity) {
			PhotoForm form = new PhotoForm();
			form.setUsername(photo.getUsername());
			form.setFolder(photo.getFolder());
			form.setThumbnail(photo.getThumbnail());
			form.setPrice(photo.getPrice());
			if (cart.contains(form)) {
				form.setIncart(true);
			}
			photos.add(form);
		}
		model.addAttribute("photos", photos);
		return "/guest/home";
	}

	@PostMapping("/cart/add")
	public ResponseEntity<String> cartAdd(@ModelAttribute PhotoForm photo, Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (!cart.contains(photo)) {
			cart.add(photo);
		}
		return ResponseEntity.ok().body(String.valueOf(cart.size()));
	}

	@GetMapping("/cart")
	public String cart(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		String[] guest = user.getUsername().split("\\.");
		model.addAttribute("username", guest[0]);
		model.addAttribute("folder", Integer.parseInt(guest[1]));
		return "/guest/cart";
	}

	@PostMapping("/cart/del")
	public ResponseEntity<String> cartDel(@ModelAttribute PhotoForm photo, Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.contains(photo)) {
			cart.remove(photo);
		}
		return ResponseEntity.ok().body(String.valueOf(cart.size()));
	}

	@GetMapping("/pay")
	public String pay(Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.isEmpty()) {
			return "redirect:/guest/home";
		}

		if (!model.containsAttribute("payForm")) {
			PayForm form = new PayForm();
			model.addAttribute("payForm", form);
		}

		int[] years = new int[10];
		int year = LocalDate.now().getYear();
		for (int i = 0; i < years.length; i++) {
			years[i] = year + i;
		}
		model.addAttribute("years", years);
		return "/guest/pay";
	}

	@PostMapping(path = "/mail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String mail(@RequestBody Map<String, Object> json) {
		String email = json.get("email").toString();
		mailHelper.sendMailReceive(email);
		return "ok";
	}

	@PostMapping("/pay")
	public String order(@ModelAttribute PayForm form, BindingResult result, Model model,
			RedirectAttributes redirectAttributes, SessionStatus sessionStatus, UriComponentsBuilder builder,
			Locale locale) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.isEmpty()) {
			return "redirect:/guest/home";
		}

		String email = form.getEmail();
		//if (email == null || email.isEmpty() || !email.matches(
		//		"^(([0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*)|(\"[^\"]*\"))@[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*$")) {
		//	result.rejectValue("email", "validation.signup.email");
		//}

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("payForm", form);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "payForm", result);
			return "redirect:/guest/pay";
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();
		String[] guest = user.getUsername().split("\\.");
		String username = guest[0];
		int folder = Integer.parseInt(guest[1]);

		// order info
		String orderno = orderService.createNumber();
		if (email == null || email.isEmpty()) {
			email = orderno;
		}
		LocalDate expiredt = LocalDate.now().plusDays(DOWNLOAD_LIMIT);
		List<String> photos = new ArrayList<String>();
		int amount = 0;

		// copy photos to order folder.
		fileHelper.createDirectory(ORDER_PATH + "/" + orderno);
		for (PhotoForm photo : cart) {
			boolean hasError = false;
			PhotoEntity entity = photoService.getPhoto(username, folder, photo.getThumbnail());
			if (entity != null && photo.getPrice() == entity.getPrice()) {
				try {
					Path srcPath = Paths.get(ORIGINAL_PATH + "/" + entity.getUsername() + "/" +
							String.valueOf(entity.getFolder()) + "/" + entity.getOriginal());
					String destFile = ORDER_PATH + "/" + orderno + "/" + entity.getOriginal();
					Files.copy(srcPath, Paths.get(destFile), StandardCopyOption.REPLACE_EXISTING);
					photos.add(destFile);
					amount = amount + entity.getPrice();
				} catch (IOException e) {
					e.printStackTrace();
					hasError = true;
				}
			} else {
				hasError = true;
			}

			if (hasError) {
				// price changed. or photo deleted.
				List<String> errors = new ArrayList<String>();
				errors.add(messageSource.getMessage("error.photo.changed", null, locale));
				redirectAttributes.addFlashAttribute("errors", errors);
				return "redirect:/guest/pay";
			}

			OrderEntity order = new OrderEntity();
			order.setOrderno(orderno);
			order.setEmail(email);
			order.setUsername(entity.getUsername());
			order.setFolder(entity.getFolder());
			order.setThumbnail(entity.getThumbnail());
			order.setOriginal(entity.getOriginal());
			order.setFilename(entity.getFilename());
			order.setPrice(entity.getPrice());
			order.setCreatedt(DateTime.now());
			order.setCharged(false);
			order.setExpiredt(expiredt);
			orderService.insertOrder(order);
		}

		// make thumbnail
		asyncService.makeThumbnail(photos);

		// charge process
		try {
			Client client = new Client(OMISE_SKEY);
			Charge charge = client.charges().create(new Charge.Create()
					.amount(amount)
					.currency("jpy")
					.capture(true)
					.description("Order No." + orderno)
					.card(form.getToken()));
			if (charge.getStatus() != ChargeStatus.Successful) {
				logger.error(charge.getFailureCode() + ": " + charge.getFailureMessage());
				asyncService.deleteOrder(orderno);
				List<String> errors = new ArrayList<String>();
				// TODO: charge.getFailureCode()を判断して、FailureMessageを多言語化
				errors.add(charge.getFailureMessage());
				redirectAttributes.addFlashAttribute("errors", errors);
				return "redirect:/guest/pay";
			} else {
				orderService.updateOrder(orderno, email);
				Transaction trans = client.transactions().get(charge.getTransaction());
				int real = (int) trans.getAmount();
				accountService.updateBalance(username, amount, real);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			asyncService.deleteOrder(orderno);
			List<String> errors = new ArrayList<String>();
			errors.add(e.getMessage());
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/guest/pay";
		}

		// send order mail
		String[] param = new String[3];
		param[0] = orderno;
		param[1] = expiredt.toString();
		String link = "/download/" + orderno + "/" + codeParser.encrypt(email);
		param[2] = builder.path(link).build().toUriString();
		mailHelper.sendOrderDone(email, param);

		sessionStatus.setComplete();
		redirectAttributes.addFlashAttribute("orderno", orderno);
		redirectAttributes.addFlashAttribute("link", link);
		return "redirect:/guest/done";
	}

	@GetMapping("/done")
	public String done(Model model) {
		if (!model.containsAttribute("orderno")) {
			return "redirect:/guest/home";
		}
		return "/guest/done";
	}
}
