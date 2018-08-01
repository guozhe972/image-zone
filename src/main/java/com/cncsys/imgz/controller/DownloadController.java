package com.cncsys.imgz.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.OrderService;

@Controller
public class DownloadController {
	@Autowired
	private CodeParser codeParser;

	@Autowired
	private OrderService orderService;

	@GetMapping("/download/{order}/{token}")
	public String home(@PathVariable("order") String order, @PathVariable("token") String token, Model model,
			HttpServletRequest request) throws ServletException {
		String email = codeParser.decrypt(token);
		if (email == null) {
			return "/system/none";
		}

		List<OrderEntity> entity = orderService.getValidOrder(order, email);
		if (entity.isEmpty()) {
			return "/system/none";
		}

		List<PhotoForm> photos = new ArrayList<PhotoForm>();
		for (OrderEntity photo : entity) {
			PhotoForm form = new PhotoForm();
			form.setUsername(order);
			form.setThumbnail(photo.getOriginal());
			photos.add(form);
		}

		request.logout();
		List<String> warns = new ArrayList<String>();
		warns.add("有効期限：" + entity.get(0).getExpiredt().toString());
		model.addAttribute("warns", warns);
		model.addAttribute("photos", photos);
		return "/system/download";
	}
}
