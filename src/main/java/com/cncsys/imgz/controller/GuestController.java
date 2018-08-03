package com.cncsys.imgz.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.cncsys.imgz.model.ChargeForm;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.AccountService;
import com.cncsys.imgz.service.AsyncService;
import com.cncsys.imgz.service.OrderService;
import com.cncsys.imgz.service.PhotoService;

@Controller
@RequestMapping("/guest")
@SessionAttributes("cart")
public class GuestController {

	public static final String FORM_MODEL_KEY = "cart";

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

	@GetMapping("/home")
	public String home(Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

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

	@PostMapping("/cart/clear")
	public String cartClear(Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		cart.clear();
		return "redirect:/guest/home";
	}

	@GetMapping("/charge")
	public String charge(@ModelAttribute ChargeForm form, Model model) {
		int[] years = new int[10];
		int year = LocalDate.now().getYear();
		for (int i = 0; i < years.length; i++) {
			years[i] = year + i;
		}
		model.addAttribute("years", years);
		return "/guest/charge";
	}

	@PostMapping(path = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String confirm(@RequestBody Map<String, Object> json) {
		String email = json.get("email").toString();
		mailHelper.sendMailConfirm(email);
		return "ok";
	}

	@PostMapping("/charge")
	public String order(@ModelAttribute ChargeForm form, BindingResult result, Model model,
			RedirectAttributes redirectAttributes, SessionStatus sessionStatus, UriComponentsBuilder builder) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> cart = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);
		if (cart.isEmpty()) {
			return "redirect:/guest/done";
		}

		// order info
		String number = orderService.createNumber();
		String email = form.getEmail();
		LocalDate expiredt = LocalDate.now().plusDays(DOWNLOAD_LIMIT);
		String username = cart.get(0).getUsername();
		List<String> photos = new ArrayList<String>();
		int total = 0;

		// copy photos to order folder.
		try {
			fileHelper.createDirectory(ORDER_PATH + "/" + number);
			for (PhotoForm photo : cart) {
				PhotoEntity entity = photoService.getPhoto(photo.getUsername(), photo.getFolder(),
						photo.getThumbnail());

				Path src = Paths.get(ORIGINAL_PATH + "/" + entity.getUsername() + "/" +
						String.valueOf(entity.getFolder()) + "/" + entity.getOriginal());
				String destFile = ORDER_PATH + "/" + number + "/" + entity.getOriginal();
				Path dest = Paths.get(destFile);
				Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
				photos.add(destFile);
				total = total + entity.getPrice();

				OrderEntity order = new OrderEntity();
				order.setNumber(number);
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
		} catch (Exception e) {
			e.printStackTrace();

			sessionStatus.setComplete();
			List<String> errors = new ArrayList<String>();
			errors.add(e.getMessage());
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/guest/charge";
		}

		// make thumbnail.
		asyncService.makeThumbnail(photos);

		//		// charge process
		//		try {
		//			Client client = new Client("skey_test_5crja3ps6nt8ihsag20");
		//			Charge charge = client.charges().create(new Charge.Create()
		//					.amount(total)
		//					.currency("jpy")
		//					.capture(true)
		//					.description("Order Number: " + order)
		//					.card(form.getToken()));
		//			logger.info("created charge: " + charge.getId());
		//			if (charge.getStatus() != ChargeStatus.Successful) {
		//				logger.info(charge.getFailureCode() + ": " + charge.getFailureMessage());
		//				// TODO: set error to RedirectAttributes
		//				return "redirect:/guest/charge";
		//			}
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			// TODO: set error to RedirectAttributes
		//			return "redirect:/guest/charge";
		//		}

		// charge success
		sessionStatus.setComplete();
		orderService.chargeOrder(number, email);
		accountService.plusBalance(username, total);

		String link = "/download/" + number + "/" + codeParser.encrypt(email);
		List<String> param = new ArrayList<String>();
		param.add(number);
		param.add(expiredt.toString());
		param.add(builder.path(link).build().toUriString());
		mailHelper.sendOrderDone(email, param);

		redirectAttributes.addFlashAttribute("order", number);
		redirectAttributes.addFlashAttribute("link", link);
		return "redirect:/guest/done";
	}

	@GetMapping("/done")
	public String done(Model model) {
		return "/guest/done";
	}
}
