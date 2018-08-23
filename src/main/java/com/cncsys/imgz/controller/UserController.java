package com.cncsys.imgz.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.entity.TransferEntity;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.model.BankForm;
import com.cncsys.imgz.model.ChangeForm;
import com.cncsys.imgz.model.ChangeForm.Input;
import com.cncsys.imgz.model.FolderForm;
import com.cncsys.imgz.model.FolderForm.Share;
import com.cncsys.imgz.model.FolderForm.Upload;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.OrderForm;
import com.cncsys.imgz.model.PhotoForm;
import com.cncsys.imgz.model.PhotoForm.Price;
import com.cncsys.imgz.service.AccountService;
import com.cncsys.imgz.service.FolderService;
import com.cncsys.imgz.service.OrderService;
import com.cncsys.imgz.service.PhotoService;
import com.cncsys.imgz.service.TransferService;
import com.cncsys.imgz.service.UploadService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Value("${default.expired.days}")
	private int DEFAULT_EXPIRED;

	@Value("${cost.transfer.fee}")
	private int COST_TRANSFER;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private AccountService accountService;

	@Autowired
	private UploadService uploadService;

	@Autowired
	private FolderService folderService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private TransferService transferService;

	@Autowired
	private PhotoService photoService;

	@Autowired
	private FolderValidator folderValidator;

	@InitBinder("folderForm")
	public void initFolderBinder(WebDataBinder binder) {
		binder.addValidators(folderValidator);
	}

	@Autowired
	private PhotoValidator photoValidator;

	@InitBinder("photoForm")
	public void initPhotoBinder(WebDataBinder binder) {
		binder.addValidators(photoValidator);
	}

	@Autowired
	private ChangeValidator changeValidator;

	@InitBinder("changeForm")
	public void initChangeBinder(WebDataBinder binder) {
		binder.addValidators(changeValidator);
	}

	@Autowired
	private BankValidator bankValidator;

	@InitBinder("bankForm")
	public void initBankBinder(WebDataBinder binder) {
		binder.addValidators(bankValidator);
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
			form.setExpiredt(folder.getExpiredt());
			folders.add(form);
		}

		model.addAttribute("folders", folders);
		return "/user/home";
	}

	@PostMapping("/rename")
	public ResponseEntity<String> rename(@RequestParam("name") String name, @RequestParam("seq") int seq,
			Locale locale) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (name == null) {
			name = "";
		} else if (name.length() > 50) {
			name = name.substring(0, 50);
		}

		String result = folderService.changeName(user.getUsername(), seq, name);
		if (result == null || result.isEmpty()) {
			result = messageSource.getMessage("default.folder.name", null, locale) + String.format("%02d", seq);
		}
		return ResponseEntity.ok().body(result);
	}

	@PostMapping(path = "/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String check(@RequestBody Map<String, Object> json) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		return String.valueOf(folderService.isLocked(user.getUsername(),
				Integer.parseInt(json.get("folder").toString())));
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
	public ResponseEntity<String> upload(@ModelAttribute @Validated(Upload.class) FolderForm form,
			BindingResult result, Locale locale) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (result.hasFieldErrors("files")) {
			return ResponseEntity.badRequest().cacheControl(CacheControl.noCache())
					.body(messageSource.getMessage(result.getFieldError("files").getCode(), null, locale));
		}

		String tempPath = UPLOAD_PATH + "/" + user.getUsername() + "/" + String.valueOf(form.getSeq()) + "/" + "temp";
		fileHelper.createDirectory(tempPath);

		// lock folder
		//folderService.lock(user.getUsername(), form.getSeq());

		int cnt = 1;
		String uuid = UUID.randomUUID().toString();
		List<String> fileList = new ArrayList<String>();
		for (MultipartFile file : form.getFiles()) {
			String fileName = file.getOriginalFilename();
			String newName = uuid + String.format("_%08d%s", cnt++, fileHelper.getExtension(fileName));
			try {
				file.transferTo(new File(tempPath + "/" + newName));
			} catch (Exception e) {
				e.printStackTrace();
			}
			fileList.add(fileName + "/" + newName);
		}
		uploadService.upload(user.getUsername(), form.getSeq(), fileList);

		// unlock folder
		//folderService.unlock(user.getUsername(), form.getSeq());

		return ResponseEntity.ok().body("ok");
	}

	@PostMapping("/clear")
	public String clear(@RequestParam("folder") int folder) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		// clear db
		folderService.initFolder(user.getUsername(), folder);

		// clear file
		String folderPath = UPLOAD_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.deleteFolder(new File(folderPath));
		String originPath = ORIGINAL_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.deleteFolder(new File(originPath));

		return "redirect:/user/home";
	}

	@GetMapping("/folder/{seq}")
	public String folder(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		FolderEntity folder = folderService.getUserFolder(user.getUsername(), seq);
		model.addAttribute("shared", folder.isShared());

		List<PhotoForm> photos = new ArrayList<PhotoForm>();
		List<PhotoEntity> entity = photoService.getPhotosByFolder(user.getUsername(), seq);
		for (PhotoEntity photo : entity) {
			PhotoForm form = new PhotoForm();
			form.setUsername(photo.getUsername());
			form.setFolder(photo.getFolder());
			form.setThumbnail(photo.getThumbnail());
			form.setPrice(photo.getPrice());
			photos.add(form);
		}

		model.addAttribute("photos", photos);
		model.addAttribute("folder", seq);
		return "/user/folder";
	}

	@PostMapping("/price")
	public ResponseEntity<String> price(@ModelAttribute @Validated(Price.class) PhotoForm form, BindingResult result,
			Locale locale) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (result.hasFieldErrors("price")) {
			FieldError errPrice = result.getFieldError("price");
			return ResponseEntity.badRequest().cacheControl(CacheControl.noCache())
					.body(messageSource.getMessage(errPrice.getCode(), errPrice.getArguments(), locale));
		}

		int price = form.getPrice();
		PhotoEntity photo = new PhotoEntity();
		photo.setPrice(price);
		photo.setUsername(user.getUsername());
		photo.setFolder(form.getFolder());
		if (!"*".equals(form.getThumbnail())) {
			photo.setThumbnail(form.getThumbnail());
			price = photoService.updatePrice(photo);
		} else {
			photoService.updateAllPrice(photo);
		}

		return ResponseEntity.ok().body(messageSource.getMessage("money.mark", null, locale) +
				String.format("%,d", price));
	}

	@PostMapping("/delete")
	public ResponseEntity<String> delete(@RequestParam("folder") int folder,
			@RequestParam("thumbnail") String thumbnail) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		// delete db
		String original = photoService.deletePhoto(user.getUsername(), folder, thumbnail);

		// delete file
		String folderPath = UPLOAD_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.deleteFile(folderPath + "/" + "preview_" + thumbnail);
		fileHelper.deleteFile(folderPath + "/" + "thumbnail_" + thumbnail);
		String originPath = ORIGINAL_PATH + "/" + user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.deleteFile(originPath + "/" + original);

		return ResponseEntity.ok().body("ok");
	}

	@GetMapping("/share/{seq}")
	public String shareGet(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		model.addAttribute("mindt", LocalDate.now());
		model.addAttribute("maxdt", LocalDate.now().plusDays(DEFAULT_EXPIRED));
		if (!model.containsAttribute("folderForm")) {
			FolderEntity folder = folderService.getUserFolder(user.getUsername(), seq);
			FolderForm form = new FolderForm();
			form.setSeq(folder.getSeq());
			form.setName(folder.getName());
			form.setGuest(folder.getGuest());
			form.setPassword("");
			form.setExpiredt(LocalDate.now().plusDays(DEFAULT_EXPIRED / 2));
			model.addAttribute("folderForm", form);
		}

		return "/user/share";
	}

	@PostMapping("/share")
	public String sharePost(@ModelAttribute @Validated(Share.class) FolderForm form,
			BindingResult result, RedirectAttributes redirectAttributes, Locale locale) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("folderForm", form);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "folderForm", result);
			return "redirect:/user/share/" + String.valueOf(form.getSeq());
		}

		FolderEntity folder = folderService.shareFolder(user.getUsername(), form.getSeq(),
				form.getPassword(), form.getExpiredt());

		String folderName = folder.getName();
		if (folderName == null || folderName.isEmpty()) {
			folderName = messageSource.getMessage("default.folder.name", null, locale)
					+ String.format("%02d", folder.getSeq());
		}

		String[] param = new String[4];
		param[0] = folderName;
		param[1] = folder.getGuest();
		param[2] = form.getPassword();
		param[3] = form.getExpiredt().toString();
		mailHelper.sendShareFolder(user.getEmail(), param);
		return "redirect:/user/home";
	}

	@GetMapping("/sales/{seq}")
	public String sales(@PathVariable("seq") int seq, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		int total = 0;
		List<OrderForm> orders = new ArrayList<OrderForm>();
		List<OrderEntity> entity = orderService.getOrderByFolder(user.getUsername(), seq);
		for (OrderEntity order : entity) {
			OrderForm form = new OrderForm();
			form.setUsername(order.getUsername());
			form.setFolder(order.getFolder());
			form.setThumbnail(order.getThumbnail());
			form.setPrice(order.getPrice());
			form.setQty(order.getQty());
			form.setAmount(order.getAmount());
			total += order.getAmount();
			orders.add(form);
		}

		model.addAttribute("orders", orders);
		model.addAttribute("total", total);
		return "/user/sales";
	}

	@GetMapping("/account")
	public String account(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		AccountEntity account = accountService.getAccountInfo(user.getUsername());
		user.setBalance(account.getBalance());
		model.addAttribute("balance", user.getBalance());

		if (!model.containsAttribute("bankForm")) {
			BankForm form = new BankForm();
			form.setActype(BankForm.AcTypes[0]);
			model.addAttribute("bankForm", form);
		}

		return "/user/account";
	}

	@PostMapping("/request")
	public String request(@ModelAttribute @Validated BankForm form, BindingResult result,
			RedirectAttributes redirectAttributes, Locale locale) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("bankForm", form);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "bankForm", result);
			return "redirect:/user/account";
		}

		if (user.getBalance() < COST_TRANSFER) {
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.account.balance", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			redirectAttributes.addFlashAttribute("bankForm", form);
			return "redirect:/user/account";
		}

		if (user.getBalance() != form.getAmount()) {
			// 改竄エラー
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.account.changed", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			redirectAttributes.addFlashAttribute("bankForm", form);
			return "redirect:/user/account";
		}

		String transno = transferService.createNumber();
		TransferEntity entity = new TransferEntity();
		entity.setTransno(transno);
		entity.setUsername(user.getUsername());
		entity.setBank(form.getBank().trim());
		entity.setBranch(form.getBranch().trim());
		entity.setActype(form.getActype());
		entity.setAcnumber(form.getAcnumber().trim());
		entity.setAcname(form.getAcname().trim());
		entity.setAmount(form.getAmount());
		entity.setCreatedt(DateTime.now());
		int balance = transferService.acceptTransfer(entity);
		if (balance < 0) {
			// 多重エラー
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.account.changed", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			redirectAttributes.addFlashAttribute("bankForm", form);
			return "redirect:/user/account";
		} else {
			user.setBalance(balance);
		}

		String[] param = new String[5];
		param[0] = transno;
		param[1] = form.getBank().trim();
		param[2] = form.getBranch().trim();
		param[3] = messageSource.getMessage("bank.account.type" + String.valueOf(form.getActype()), null, locale) + " "
				+ form.getAcnumber().trim();
		param[4] = String.format("%,d", form.getAmount() - COST_TRANSFER)
				+ messageSource.getMessage("money.unit", null, locale);
		mailHelper.sendTransAccept(user.getEmail(), param);

		List<String> infos = new ArrayList<String>();
		infos.add(messageSource.getMessage("info.transfer.accept", new Object[] { transno }, locale));
		redirectAttributes.addFlashAttribute("infos", infos);
		return "redirect:/user/account";
	}

	@GetMapping("/change")
	public String password(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (!model.containsAttribute("changeForm")) {
			ChangeForm form = new ChangeForm();
			form.setUsername(user.getUsername());
			form.setEmail(user.getEmail());
			model.addAttribute("changeForm", form);
		}
		return "/user/change";
	}

	@PostMapping("/change")
	public String change(@ModelAttribute @Validated(Input.class) ChangeForm form, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("changeForm", form);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "changeForm", result);
			return "redirect:/user/change";
		}

		String email = accountService.changePassword(user.getUsername(), form.getPassword());
		String[] param = new String[2];
		param[0] = form.getUsername();
		param[1] = form.getPassword();
		mailHelper.sendChangeSuccess(email, param);
		return "redirect:/user/change?info";
	}
}
