package com.cncsys.imgz.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDate;
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
import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.model.FolderForm;
import com.cncsys.imgz.model.FolderForm.Upload;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.service.AsyncService;
import com.cncsys.imgz.service.FolderService;
import com.cncsys.imgz.service.PhotoService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private FolderService folderService;

	@Autowired
	private PhotoService photoService;

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
		List<FolderEntity> entity = folderService.getUserFolders(user.getUsername());
		for (FolderEntity folder : entity) {
			FolderForm form = new FolderForm();
			form.setSeq(folder.getSeq());
			form.setName(folder.getName());
			form.setShared(folder.isShared());
			if (folder.isShared()) {
				// TODO: form.setExpiredt(expiredt of guest);
			}
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

		String tempPath = UPLOAD_PATH + "/" + user.getUsername() + "/" + form.getSeq() + "/" + "temp";
		fileHelper.createDirectory(tempPath);

		// lock folder
		folderService.lock(user.getUsername(), form.getSeq());

		int cnt = 1;
		String uuid = UUID.randomUUID().toString();
		List<String> fileList = new ArrayList<String>();
		for (MultipartFile file : form.getFiles()) {
			String fileName = file.getOriginalFilename();
			fileName = uuid + String.format("_%08d%s", cnt++, fileHelper.getExtension(fileName));
			String uploadFile = tempPath + "/" + fileName;
			try {
				file.transferTo(new File(uploadFile));
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			fileList.add(uploadFile);
		}
		asyncService.upload(user.getUsername(), form.getSeq(), fileList);

		return ResponseEntity.ok().body(json);
	}

	@GetMapping("/folder/{seq}")
	public String folder(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		List<PhotoForm> photos = new ArrayList<PhotoForm>();
		File folderPath = new File(UPLOAD_PATH + "/" + user.getUsername() + "/" + String.valueOf(seq));
		if (folderPath.exists()) {
			List<PhotoEntity> entity = photoService.getPhotosByFolder(user.getUsername(), seq);
			for (PhotoEntity photo : entity) {
				PhotoForm form = new PhotoForm();
				form.setUsername(photo.getUsername());
				form.setFolder(photo.getFolder());
				form.setThumbnail(photo.getThumbnail());
				form.setPrice(photo.getPrice());
				photos.add(form);
			}
		}

		model.addAttribute("photos", photos);
		model.addAttribute("folder", seq);
		return "/user/folder";
	}

	@PostMapping("/price")
	public ResponseEntity<String> price(@ModelAttribute @Validated PhotoForm form, BindingResult result,
			Locale locale) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		PhotoEntity photo = new PhotoEntity();
		photo.setUsername(user.getUsername());
		photo.setFolder(form.getFolder());
		photo.setThumbnail(form.getThumbnail());
		photo.setPrice(form.getPrice());
		int price = photoService.updatePrice(photo);

		return ResponseEntity.ok().body(String.format("%,d", price));
	}

	@PostMapping("/delete")
	public ResponseEntity<String> delete(@RequestParam("folder") int folder,
			@RequestParam("thumbnail") String thumbnail) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		String original = photoService.deletePhoto(user.getUsername(), folder, thumbnail);
		String folderPath = UPLOAD_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.delete(new File(folderPath + "/" + "preview_" + thumbnail));
		fileHelper.delete(new File(folderPath + "/" + "thumbnail_" + thumbnail));
		String originPath = ORIGINAL_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.delete(new File(originPath + "/" + original));

		return ResponseEntity.ok().body("ok");
	}

	@GetMapping("/share/{seq}")
	public String shareGet(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		FolderEntity folder = folderService.getUserFolder(user.getUsername(), seq);
		FolderForm form = new FolderForm();
		form.setSeq(folder.getSeq());
		form.setName(folder.getName());
		form.setGuest(folder.getGuest());

		model.addAttribute("folder", folder);
		return "redirect:/user/share";
	}

	@PostMapping("/share")
	public String sharePost(@RequestParam("folder") int folder, RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		folderService.shareFolder(user.getUsername(), folder, "1234", LocalDate.now().plusDays(30));

		return "redirect:/user/home";
	}

	@PostMapping("/clear")
	public String clear(@RequestParam("folder") int folder, RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		// error test ===============
		if (folder == 3) {
			List<String> errors = new ArrayList<String>();
			errors.add("あいうえお error 1");
			errors.add("あいうえお error 2");
			errors.add("あいうえお error 3");
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/user/home";
		}
		// error test ===============

		// clear db
		folderService.initFolder(user.getUsername(), folder);

		// clear file
		String folderPath = UPLOAD_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.delete(new File(folderPath));
		String originPath = ORIGINAL_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.delete(new File(originPath));

		return "redirect:/user/home";
	}
}
