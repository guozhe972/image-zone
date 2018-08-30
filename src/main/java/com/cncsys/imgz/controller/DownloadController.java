package com.cncsys.imgz.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.model.OrderForm;
import com.cncsys.imgz.service.OrderService;

@Controller
public class DownloadController {

	@Autowired
	private CodeParser codeParser;

	@Autowired
	private OrderService orderService;

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/download/{order}/{token}")
	public String download(@PathVariable("order") String order, @PathVariable("token") String token,
			Model model, Locale locale) {
		String email = codeParser.decrypt(token);
		if (email == null) {
			return "/system/none";
		}

		List<OrderEntity> entity = orderService.getValidOrder(order, email);
		if (entity.isEmpty()) {
			return "/system/none";
		}

		List<OrderForm> photos = new ArrayList<OrderForm>();
		for (OrderEntity photo : entity) {
			OrderForm form = new OrderForm();
			form.setOrderno(order);
			form.setThumbnail(photo.getOriginal());
			photos.add(form);
		}
		model.addAttribute("photos", photos);

		LocalDate expiredt = entity.get(0).getExpiredt();
		List<String> errors = new ArrayList<String>();
		errors.add(messageSource.getMessage("message.download.expiry",
				new Object[] { expiredt.toString() },
				locale));
		model.addAttribute("errors", errors);
		return "/system/download";
	}
}
