package com.cncsys.imgz.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.model.FolderForm;
import com.cncsys.imgz.model.FolderForm.Upload;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.AsyncService;
import com.cncsys.imgz.service.FolderService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private FolderService folderService;

	@Autowired
	private FolderValidator uploadValidator;

	@InitBinder("folderForm")
	public void initUploadBinder(WebDataBinder binder) {
		binder.addValidators(uploadValidator);
	}

	@GetMapping("/home")
	public String home(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		List<FolderForm> folders = new ArrayList<FolderForm>();
		List<FolderEntity> entity = folderService.getUserFolderList(user.getUsername());
		for (FolderEntity folder : entity) {
			FolderForm form = new FolderForm();
			form.setSeq(folder.getSeq());
			form.setName(folder.getName());
			folders.add(form);
		}

		model.addAttribute("folders", folders);
		return "/user/home";
	}

	//	@PostMapping("/upload")
	//	public SseEmitter upload1(MultipartRequest multipartRequest) throws IOException {
	//		SseEmitter emitter = new SseEmitter();
	//		for (MultipartFile file : multipartRequest.getFiles("files")) {
	//			asyncService.upload(emitter, file);
	//		}
	//		return emitter;
	//	}

	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> upload(@ModelAttribute @Validated(Upload.class) FolderForm form,
			BindingResult result, Locale locale) {
		Map<String, Object> json = new HashMap<String, Object>();

		if (result.hasErrors()) {
			List<String> errors = new ArrayList<String>();
			for (ObjectError err : result.getAllErrors()) {
				errors.add(messageSource.getMessage(err.getCode(), null, locale));
			}
			json.put("errors", errors);
			return ResponseEntity.badRequest().cacheControl(CacheControl.noCache()).body(json);
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		File tempPath = new File(UPLOAD_PATH + File.separator + user.getUsername());
		if (!tempPath.exists()) {
			tempPath.mkdirs();
		}

		int cnt = 1;
		String uuid = UUID.randomUUID().toString();
		List<String> fileList = new ArrayList<String>();
		for (MultipartFile file : form.getFiles()) {
			String fileName = file.getOriginalFilename();
			fileName = uuid + String.format("_%08d%s", cnt++, fileName.substring(fileName.lastIndexOf(".")));
			String uploadFile = tempPath.getPath() + File.separator + fileName;
			try {
				file.transferTo(new File(uploadFile));
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			fileList.add(uploadFile);
		}
		asyncService.upload(user, form.getSeq(), fileList);

		return ResponseEntity.ok().body(json);
	}

	@PostMapping("/clear")
	public String clear(@RequestParam("folder") int folder, RedirectAttributes redirectAttributes) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		File folderPath = new File(
				UPLOAD_PATH + File.separator + user.getUsername() + File.separator + String.valueOf(folder));

		if (folder == 3) {
			List<String> errors = new ArrayList<String>();
			errors.add("あいうえお error 1");
			errors.add("あいうえお error 2");
			errors.add("あいうえお error 3");
			redirectAttributes.addFlashAttribute("errors", errors);
		} else {
			if (folderPath.exists()) {
				for (File child : folderPath.listFiles()) {
					child.delete();
				}
				folderPath.delete();
			}
		}

		return "redirect:/user/home";
	}

	@GetMapping("/folder/{seq}")
	public String folder(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		File folderPath = new File(
				UPLOAD_PATH + File.separator + user.getUsername() + File.separator + String.valueOf(seq));

		List<PhotoForm> photos = new ArrayList<PhotoForm>();
		if (folderPath.exists()) {
			for (File image : folderPath.listFiles()) {
				if (image.getName().startsWith("thumbnail_")) {
					PhotoForm form = new PhotoForm();
					form.setLink(user.getUsername() + "/" + String.valueOf(seq) + "/" + image.getName());
					form.setPrice(200);
					photos.add(form);
				}
			}
		}

		model.addAttribute("photos", photos);
		return "/user/folder";
	}

}
