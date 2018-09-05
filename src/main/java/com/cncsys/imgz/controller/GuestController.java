package com.cncsys.imgz.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import co.omise.Client;
import co.omise.ClientException;
import co.omise.models.Charge;
import co.omise.models.ChargeStatus;
import co.omise.models.OmiseException;
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

	@Value("${order.download.days}")
	private int DOWNLOAD_DAYS;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Value("${order.file.path}")
	private String ORDER_PATH;

	@Value("${omise.secret.key}")
	private String OMISE_SKEY;

	@Value("${omise.charge.min}")
	private int OMISE_MIN;

	@Value("${omise.charge.max}")
	private int OMISE_MAX;

	@Value("${alipay.app.id}")
	private String ALIPAY_APPID;

	@Value("${alipay.public.key}")
	private String ALIPAY_PUBLIC;

	@Value("${alipay.private.key}")
	private String ALIPAY_PRIVATE;

	@Value("${alipay.gateway.url}")
	private String ALIPAY_GATEWAY;

	@Value("${alipay.product.subject}")
	private String ALIPAY_SUBJECT;

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
	public String pay(Model model, Locale locale) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.isEmpty()) {
			return "redirect:/guest/home";
		}

		int total = 0;
		for (PhotoForm photo : cart) {
			total += photo.getPrice();
		}
		model.addAttribute("total", total);

		if (!model.containsAttribute("payForm")) {
			PayForm form = new PayForm();
			model.addAttribute("payForm", form);
		}

		String view = "/system/none";
		if ("zh".equals(locale.getLanguage())) {
			view = "/guest/zhpay";

		} else if ("ja".equals(locale.getLanguage())) {
			view = "/guest/credit";

			if (total < OMISE_MIN || OMISE_MAX < total) {
				if (!model.containsAttribute("errors")) {
					List<String> errors = new ArrayList<String>();
					errors.add(messageSource.getMessage("error.charge.range",
							new Object[] { String.format("%,d", OMISE_MIN), String.format("%,d", OMISE_MAX) }, locale));
					model.addAttribute("errors", errors);
				}
			}

			int[] years = new int[10];
			int year = LocalDate.now().getYear();
			for (int i = 0; i < years.length; i++) {
				years[i] = year + i;
			}
			model.addAttribute("years", years);
		}

		return view;
	}

	@PostMapping(path = "/mail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String mail(@RequestBody Map<String, Object> json) {
		String email = json.get("email").toString();
		mailHelper.sendMailReceive(email);
		return "ok";
	}

	@PostMapping("/alipay")
	public void alipay(@ModelAttribute PayForm form, BindingResult result, Model model,
			RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response,
			UriComponentsBuilder builder, Locale locale) throws AlipayApiException, IOException {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/guest/home");
			return;
		}

		String email = form.getEmail();
		if (email == null) {
			email = "";
		} else {
			email = email.trim();
		}

		if (email.isEmpty() || !email.matches(
				"^(([0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*)|(\"[^\"]*\"))@[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*$")) {
			result.rejectValue("email", "validation.signup.email");
		}
		if (result.hasErrors()) {
			FlashMap flashMap = new FlashMap();
			flashMap.put("payForm", form);
			flashMap.put(BindingResult.MODEL_KEY_PREFIX + "payForm", result);

			String redirectPath = request.getContextPath() + "/guest/pay";
			flashMap.setTargetRequestPath(redirectPath);
			FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
			response.sendRedirect(redirectPath);
			return;
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();
		String[] guest = user.getUsername().split("\\.");
		String username = guest[0];
		int folder = Integer.parseInt(guest[1]);

		// order info
		int amount = 0;
		String orderno = orderService.createNumber();
		if (email.isEmpty()) {
			email = orderno;
		}

		// copy photos to order folder
		List<String> photos = new ArrayList<String>();
		fileHelper.createDirectory(ORDER_PATH + "/" + orderno);
		for (PhotoForm photo : cart) {
			boolean hasError = false;
			PhotoEntity entity = photoService.getPhoto(username, folder, photo.getThumbnail());
			if (entity != null) {
				if (photo.getPrice() == entity.getPrice()) {
					try {
						Path srcPath = Paths.get(ORIGINAL_PATH + "/" + entity.getUsername() + "/" +
								String.valueOf(entity.getFolder()) + "/" + entity.getOriginal());
						String destFile = ORDER_PATH + "/" + orderno + "/" + entity.getOriginal();
						Files.copy(srcPath, Paths.get(destFile), StandardCopyOption.REPLACE_EXISTING);
						photos.add(destFile);
					} catch (IOException e) {
						e.printStackTrace();
						hasError = true;
						cart.remove(photo);
					}
				} else {
					hasError = true;
					photo.setPrice(entity.getPrice());
				}
			} else {
				hasError = true;
				cart.remove(photo);
			}

			if (hasError) {
				// price changed. or photo deleted. or no disk space.
				List<String> errors = new ArrayList<String>();
				errors.add(messageSource.getMessage("error.photo.changed", null, locale));

				FlashMap flashMap = new FlashMap();
				flashMap.put("errors", errors);

				String redirectPath = request.getContextPath() + "/guest/pay";
				flashMap.setTargetRequestPath(redirectPath);
				FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
				response.sendRedirect(redirectPath);
				return;
			} else {
				amount += entity.getPrice();
			}

			// insert uncharged order
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
			order.setExpiredt(null);
			orderService.insertOrder(order);
		}

		// make thumbnail
		asyncService.makeThumbnail(photos);

		// payment process (go to alipay)
		AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_GATEWAY, ALIPAY_APPID, ALIPAY_PRIVATE, "json",
				"utf-8", ALIPAY_PUBLIC, "RSA2");

		String returnUrl = builder.cloneBuilder().path("/guest/alipay/" + codeParser.encrypt(email)).build()
				.toUriString();
		String notifyUrl = builder.cloneBuilder().path("/alipay/notify").build().toUriString();
		AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
		alipayRequest.setReturnUrl(returnUrl);
		alipayRequest.setNotifyUrl(notifyUrl);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("out_trade_no", orderno);
		paramMap.put("total_amount", String.valueOf(amount) + ".00");
		paramMap.put("subject", ALIPAY_SUBJECT);
		paramMap.put("passback_params", codeParser.encrypt(email));
		paramMap.put("timeout_express", "20m");
		paramMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
		ObjectMapper objMapper = new ObjectMapper();
		alipayRequest.setBizContent(objMapper.writeValueAsString(paramMap));

		response.setContentType("text/html;charset=utf-8");
		response.getWriter().write(alipayClient.pageExecute(alipayRequest).getBody());
		response.getWriter().flush();
		response.getWriter().close();
	}

	@GetMapping("/alipay/{token}")
	public String aliReturn(@PathVariable("token") String token, RedirectAttributes redirectAttributes,
			HttpServletRequest request, SessionStatus sessionStatus, Locale locale)
			throws AlipayApiException {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}

		String orderno = request.getParameter("out_trade_no");
		String downlink = "/download/" + orderno + "/" + token;

		boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC, "utf-8", "RSA2");
		if (!signVerified) {
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.payment.failed", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/guest/pay";
		}

		sessionStatus.setComplete();
		redirectAttributes.addFlashAttribute("orderno", orderno);
		redirectAttributes.addFlashAttribute("downlink", downlink);
		return "redirect:/guest/done";
	}

	@PostMapping("/credit")
	public String credit(@ModelAttribute PayForm form, BindingResult result, Model model,
			RedirectAttributes redirectAttributes, SessionStatus sessionStatus, UriComponentsBuilder builder,
			Locale locale) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.isEmpty()) {
			return "redirect:/guest/home";
		}

		String email = form.getEmail();
		if (email == null) {
			email = "";
		} else {
			email = email.trim();
		}

		if (email.isEmpty() || !email.matches(
				"^(([0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*)|(\"[^\"]*\"))@[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+(\\.[0-9a-zA-Z!#\\$%&'\\*\\+\\-/=\\?\\^_`\\{\\}\\|~]+)*$")) {
			result.rejectValue("email", "validation.signup.email");
		}
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
		int amount = 0;
		String orderno = orderService.createNumber();
		if (email.isEmpty()) {
			email = orderno;
		}

		// copy photos to order folder
		List<String> photos = new ArrayList<String>();
		fileHelper.createDirectory(ORDER_PATH + "/" + orderno);
		for (PhotoForm photo : cart) {
			boolean hasError = false;
			PhotoEntity entity = photoService.getPhoto(username, folder, photo.getThumbnail());
			if (entity != null) {
				if (photo.getPrice() == entity.getPrice()) {
					try {
						Path srcPath = Paths.get(ORIGINAL_PATH + "/" + entity.getUsername() + "/" +
								String.valueOf(entity.getFolder()) + "/" + entity.getOriginal());
						String destFile = ORDER_PATH + "/" + orderno + "/" + entity.getOriginal();
						Files.copy(srcPath, Paths.get(destFile), StandardCopyOption.REPLACE_EXISTING);
						photos.add(destFile);
					} catch (IOException e) {
						e.printStackTrace();
						hasError = true;
						cart.remove(photo);
					}
				} else {
					hasError = true;
					photo.setPrice(entity.getPrice());
				}
			} else {
				hasError = true;
				cart.remove(photo);
			}

			if (hasError) {
				// price changed. or photo deleted. or no disk space.
				List<String> errors = new ArrayList<String>();
				errors.add(messageSource.getMessage("error.photo.changed", null, locale));
				redirectAttributes.addFlashAttribute("errors", errors);
				return "redirect:/guest/pay";
			} else {
				amount += entity.getPrice();
			}

			// insert uncharged order
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
			order.setExpiredt(null);
			orderService.insertOrder(order);
		}

		// make thumbnail
		asyncService.makeThumbnail(photos);

		// payment process
		Client client = null;
		Charge charge = null;
		try {
			client = new Client(OMISE_SKEY);
			charge = client.charges().create(new Charge.Create()
					.amount(amount)
					.currency("jpy")
					.capture(true)
					.description("Order No." + orderno)
					.card(form.getToken()));
			if (charge.getStatus() != ChargeStatus.Successful) {
				asyncService.deleteOrder(orderno);
				List<String> errors = new ArrayList<String>();
				errors.add(messageSource.getMessage("error.payment.failed", null, locale));
				redirectAttributes.addFlashAttribute("errors", errors);
				return "redirect:/guest/pay";
			} else {
				logger.info("Order No." + orderno + " Charge Successful.");
			}
		} catch (ClientException | IOException | OmiseException e) {
			logger.error("Omise Exception Occurred.");
			e.printStackTrace();
			asyncService.deleteOrder(orderno);
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.omise.exception", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/guest/pay";
		}

		// charge success
		LocalDate expiredt = LocalDate.now().plusDays(DOWNLOAD_DAYS);
		orderService.updateOrder(orderno, email, expiredt);
		String downlink = "/download/" + orderno + "/" + codeParser.encrypt(email);

		// send order mail
		if (!email.equals(orderno)) {
			String[] param = new String[3];
			param[0] = orderno;
			param[1] = expiredt.toString();
			param[2] = builder.path(downlink).build().toUriString();
			mailHelper.sendOrderDone(email, param);
		}

		// update balance
		//accountService.updateBalance(username, amount, new BigDecimal(amount));
		try {
			Transaction trans = client.transactions().get(charge.getTransaction());
			accountService.updateBalance(username, amount, new BigDecimal(trans.getAmount()));
		} catch (IOException | OmiseException e) {
			logger.error("Order No." + orderno + " User Balance Update Error.");
			e.printStackTrace();
			// TODO: send mail to admin
		}

		sessionStatus.setComplete();
		redirectAttributes.addFlashAttribute("orderno", orderno);
		redirectAttributes.addFlashAttribute("downlink", downlink);
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
