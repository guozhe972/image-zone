package com.cncsys.imgz.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.model.ChargeForm;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.PhotoService;

import co.omise.Client;
import co.omise.models.Charge;
import co.omise.models.ChargeStatus;

@Controller
@RequestMapping("/guest")
@SessionAttributes("cart")
public class GuestController {
	private static final Logger logger = LoggerFactory.getLogger(GuestController.class);

	public static final String FORM_MODEL_KEY = "cart";

	@ModelAttribute(FORM_MODEL_KEY)
	public List<PhotoForm> initCartList() {
		List<PhotoForm> cart = new ArrayList<PhotoForm>();
		return cart;
	}

	@Autowired
	private PhotoService photoService;

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

	@PostMapping("/charge")
	public String order(@ModelAttribute ChargeForm form, BindingResult result,
			RedirectAttributes redirectAttributes) {
		logger.info("email: " + form.getEmail());
		// TODO: insert order. charged = false
		String order = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS").print(DateTime.now());
		int price = 1000;

		try {
			Client client = new Client("skey_test_5crja3ps6nt8ihsag20");
			Charge charge = client.charges().create(new Charge.Create()
					.amount(price)
					.currency("jpy")
					.capture(true)
					.description("Order Number: " + order)
					.card(form.getToken()));
			logger.info("created charge: " + charge.getId());
			if (charge.getStatus() == ChargeStatus.Successful) {
				// TODO: update order. charged = true
			} else {
				logger.info(charge.getFailureCode() + ": " + charge.getFailureMessage());
				// TODO: set error to RedirectAttributes
				return "redirect:/guest/charge";
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: set error to RedirectAttributes
			return "redirect:/guest/charge";
		}
		return "redirect:/guest/download";
	}

	@GetMapping("/download")
	public String down(Model model) {
		return "/guest/download";
	}
}
