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

		String tempPath = UPLOAD_PATH + "/" +
				user.getUsername() + "/" + form.getSeq() + "/" + "temp";
		fileHelper.createDirectory(tempPath);

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
		asyncService.upload(user, form.getSeq(), fileList);

		return ResponseEntity.ok().body(json);
	}

	@PostMapping("/clear")
	public String clear(@RequestParam("folder") int folder, RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (folder == 3) {
			List<String> errors = new ArrayList<String>();
			errors.add("あいうえお error 1");
			errors.add("あいうえお error 2");
			errors.add("あいうえお error 3");
			redirectAttributes.addFlashAttribute("errors", errors);
		} else {
			photoService.clearFolder(user.getUsername(), folder);
			File folderPath = new File(UPLOAD_PATH + "/" +
					user.getUsername() + "/" + String.valueOf(folder));
			fileHelper.delete(folderPath);

			File originPath = new File(ORIGINAL_PATH + "/" +
					user.getUsername() + "/" + String.valueOf(folder));
			if (originPath.exists()) {
				List<String> lstSold = new ArrayList<String>();
				// TODO: 購入済みの写真リストを取得する
				for (File child : originPath.listFiles()) {
					if (!lstSold.contains(child.getName()))
						child.delete();
				}
			}
		}

		return "redirect:/user/home";
	}

	@GetMapping("/folder/{seq}")
	public String folder(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		List<PhotoForm> photos = new ArrayList<PhotoForm>();

		File folderPath = new File(UPLOAD_PATH + "/" +
				user.getUsername() + "/" + String.valueOf(seq));
		if (folderPath.exists()) {
			List<PhotoEntity> lstPhoto = photoService.selectPhotoByUser(user.getUsername(), seq);
			for (PhotoEntity photo : lstPhoto) {
				PhotoForm form = new PhotoForm();
				form.setThumbnail(photo.getThumbnail());
				form.setLink(user.getUsername() + "/" + String.valueOf(seq) + "/thumbnail_" + photo.getThumbnail());
				form.setPrice(200);
				photos.add(form);
			}
		}

		model.addAttribute("photos", photos);
		model.addAttribute("folder", seq);
		return "/user/folder";
	}

	@PostMapping("/delete")
	public ResponseEntity<String> delete(@RequestParam("folder") int folder,
			@RequestParam("thumbnail") String thumbnail) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		String original = photoService.deletePhoto(user.getUsername(), folder, thumbnail);
		String folderPath = UPLOAD_PATH + "/" +
				user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.delete(new File(folderPath + "/" + "preview_" + thumbnail));
		fileHelper.delete(new File(folderPath + "/" + "thumbnail_" + thumbnail));

		// TODO: 該当写真が購入されている場合は削除しない
		String originPath = ORIGINAL_PATH + "/" +
				user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.delete(new File(originPath + "/" + original));

		return ResponseEntity.ok().body("ok");
	}

}
