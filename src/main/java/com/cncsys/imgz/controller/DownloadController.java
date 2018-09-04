package com.cncsys.imgz.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.model.OrderForm;
import com.cncsys.imgz.service.OrderService;

@Controller
public class DownloadController {

	@Value("${order.file.path}")
	private String ORDER_PATH;

	@Autowired
	private CodeParser codeParser;

	@Autowired
	private OrderService orderService;

	@Autowired
	private FileHelper fileHelper;

	@GetMapping("/download/{order}/{token}")
	public String download(@PathVariable("order") String order, @PathVariable("token") String token, Model model) {
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
		model.addAttribute("link", order + "/" + token);
		model.addAttribute("expiredt", entity.get(0).getExpiredt());
		return "/system/download";
	}

	@GetMapping("/download/img/{seq}/{order}")
	public ResponseEntity<byte[]> img(@PathVariable("seq") int seq, @PathVariable("order") String order,
			@RequestParam("file") String file) throws IOException {
		String filePath = ORDER_PATH + "/" + order + "/" + file;

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.IMAGE_JPEG);
		responseHeaders.set("Content-Disposition",
				"attachment;filename=IMG" + String.format("%05d", seq) + fileHelper.getExtension(file));
		return new ResponseEntity<>(Files.readAllBytes(Paths.get(filePath)), responseHeaders, HttpStatus.OK);
	}

	@GetMapping("/download/zip/{order}/{token}")
	public ResponseEntity<StreamingResponseBody> zip(@PathVariable("order") String order,
			@PathVariable("token") String token) {
		List<String> photos = new ArrayList<String>();
		List<OrderEntity> entity = orderService.getValidOrder(order, codeParser.decrypt(token));
		for (OrderEntity photo : entity) {
			photos.add(ORDER_PATH + "/" + order + "/" + photo.getOriginal());
		}

		StreamingResponseBody responseBody = new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream outputStream) throws IOException {
				try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(outputStream));) {
					for (int i = 0; i < photos.size(); i++) {
						try (InputStream bis = new BufferedInputStream(new FileInputStream(photos.get(i)));) {
							zos.putNextEntry(new ZipEntry(
									"IMG" + String.format("%05d", i + 1) + fileHelper.getExtension(photos.get(i))));
							int len;
							byte[] buf = new byte[1024];
							while ((len = bis.read(buf)) != -1) {
								zos.write(buf, 0, len);
							}
							zos.closeEntry();
						}
					}
				}
			}
		};

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "application/zip");
		responseHeaders.set("Content-Disposition", "attachment; filename=" + order + ".zip");
		return new ResponseEntity<StreamingResponseBody>(responseBody, responseHeaders, HttpStatus.OK);
	}

}
