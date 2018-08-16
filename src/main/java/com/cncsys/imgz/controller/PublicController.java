package com.cncsys.imgz.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.PhotoService;

@Controller
@RequestMapping("/public")
@SessionAttributes("basket")
public class PublicController {
	//private static final Logger logger = LoggerFactory.getLogger(PublicController.class);
	private static final String FORM_MODEL_KEY = "basket";

	@ModelAttribute(FORM_MODEL_KEY)
	public List<PhotoForm> initCartList() {
		List<PhotoForm> basket = new ArrayList<PhotoForm>();
		return basket;
	}

	@Autowired
	private PhotoService photoService;

	@GetMapping("/home")
	public String home(Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> basket = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);

		List<PhotoForm> photos = new ArrayList<PhotoForm>();
		List<PhotoEntity> entity = photoService.getPhotosByPublic();
		for (PhotoEntity photo : entity) {
			PhotoForm form = new PhotoForm();
			form.setUsername(photo.getUsername());
			form.setFolder(photo.getFolder());
			form.setThumbnail(photo.getThumbnail());
			form.setPrice(photo.getPrice());
			if (basket.contains(form)) {
				form.setIncart(true);
			}
			photos.add(form);
		}

		model.addAttribute("photos", photos);
		return "/public/home";
	}

	@PostMapping("/basket/add")
	public ResponseEntity<String> basketAdd(@ModelAttribute PhotoForm photo, Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> basket = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);

		if (!basket.contains(photo)) {
			basket.add(photo);
		}
		return ResponseEntity.ok().body(String.valueOf(basket.size()));
	}

	@GetMapping("/basket")
	public String basket(Model model) {
		return "/public/basket";
	}

	@PostMapping("/basket/del")
	public ResponseEntity<String> basketDel(@ModelAttribute PhotoForm photo, Model model) {
		@SuppressWarnings("unchecked")
		List<PhotoForm> basket = (List<PhotoForm>) model.asMap().get(FORM_MODEL_KEY);

		if (basket.contains(photo)) {
			basket.remove(photo);
		}
		return ResponseEntity.ok().body(String.valueOf(basket.size()));
	}
}
