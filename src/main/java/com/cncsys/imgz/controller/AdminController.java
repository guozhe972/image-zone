package com.cncsys.imgz.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.entity.TransferEntity;
import com.cncsys.imgz.helper.CodeParser;
import com.cncsys.imgz.helper.MailHelper;
import com.cncsys.imgz.model.BankForm;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.model.OrderForm;
import com.cncsys.imgz.service.AccountService;
import com.cncsys.imgz.service.OrderService;
import com.cncsys.imgz.service.TransferService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Value("${admin.username}")
	private String ADMIN_NAME;

	@Value("${order.download.days}")
	private int DOWNLOAD_DAYS;

	@Autowired
	private MailHelper mailHelper;

	@Autowired
	private CodeParser codeParser;

	@Autowired
	private AccountService accountService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private TransferService transferService;

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/home")
	public String home(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		LoginUser user = (LoginUser) auth.getPrincipal();

		AccountEntity account = accountService.getAccountInfo(user.getUsername());
		user.setBalance(account.getBalance());
		model.addAttribute("balance", user.getBalance());

		List<BankForm> trans = new ArrayList<BankForm>();
		List<TransferEntity> entity = transferService.getRequest(false);
		for (TransferEntity transfer : entity) {
			BankForm form = new BankForm();
			form.setTransno(transfer.getTransno());
			form.setBank(transfer.getBank());
			form.setBranch(transfer.getBranch());
			form.setActype(transfer.getActype());
			form.setAcnumber(transfer.getAcnumber());
			form.setAcname(transfer.getAcname());
			form.setAmount(transfer.getAmount());
			trans.add(form);
		}

		model.addAttribute("transfer", trans);
		return "/admin/home";
	}

	@PostMapping("/transfer")
	public String transfer(@RequestParam("transno") String transno, @RequestParam("fee") int fee) {
		transferService.doneTransfer(transno, Math.abs(fee));
		return "redirect:/admin/home";
	}

	@GetMapping("/order")
	public String order(Model model) {
		return "/admin/order";
	}

	@PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody OrderForm search(@RequestBody Map<String, Object> json) {
		String orderno = json.get("orderno").toString();

		int amount = 0;
		List<OrderEntity> order = orderService.searchOrder(orderno);
		for (OrderEntity entity : order) {
			amount += entity.getPrice();
		}

		OrderForm form = new OrderForm();
		if (amount > 0) {
			form.setOrderno(orderno);
			form.setEmail(order.get(0).getEmail());
			form.setAmount(amount);
			form.setCharged(order.get(0).isCharged());
		}
		return form;
	}

	@PostMapping("/charge")
	public String charge(@RequestParam("orderno") String orderno, RedirectAttributes redirectAttributes,
			UriComponentsBuilder builder, Locale locale) {
		if (orderno == null || orderno.isEmpty()) {
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.order.none", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/admin/order";
		}

		int amount = 0;
		List<OrderEntity> order = orderService.searchOrder(orderno);
		for (OrderEntity entity : order) {
			amount += entity.getPrice();
		}

		if (amount > 0) {
			String email = order.get(0).getEmail();
			boolean isCharged = order.get(0).isCharged();
			LocalDate expiredt = (isCharged ? order.get(0).getExpiredt() : LocalDate.now().plusDays(DOWNLOAD_DAYS));
			if (!isCharged) {
				orderService.updateOrder(orderno, email, expiredt);
			}

			// send order mail
			if (!email.equals(orderno)) {
				String[] param = new String[3];
				param[0] = orderno;
				param[1] = expiredt.toString();
				String downlink = "/download/" + orderno + "/" + codeParser.encrypt(email);
				param[2] = builder.path(downlink).build().toUriString();
				mailHelper.sendOrderDone(email, param);
			}

			if (!isCharged) {
				// update balance
				String username = order.get(0).getUsername();
				accountService.updateBalance(username, amount, BigDecimal.valueOf(amount));
			}

			List<String> infos = new ArrayList<String>();
			infos.add(messageSource.getMessage("info.order.done", new Object[] { orderno }, locale));
			redirectAttributes.addFlashAttribute("infos", infos);
		} else {
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.order.none", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
		}
		return "redirect:/admin/order";
	}

	@GetMapping("/vip")
	public String vip(Model model) {
		model.addAttribute("minExpiredt", LocalDate.now());
		model.addAttribute("expiredt", LocalDate.now().plusYears(1));
		model.addAttribute("maxExpiredt", LocalDate.now().plusYears(10));
		return "/admin/vip";
	}

	@PostMapping("/upgrade")
	public String upgrade(@RequestParam("username") String username,
			@RequestParam("expiredt") @DateTimeFormat(iso = ISO.DATE) LocalDate expiredt,
			RedirectAttributes redirectAttributes, Locale locale) {
		if (username == null || username.isEmpty()) {
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.signin.username", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:/admin/vip";
		}

		String email = accountService.updateVip(username, expiredt);
		if (email != null) {
			String[] param = new String[2];
			param[0] = username;
			param[1] = expiredt.toString();
			mailHelper.sendVipUpgrade(email, param);

			List<String> infos = new ArrayList<String>();
			infos.add(messageSource.getMessage("info.vip.success", new Object[] { username }, locale));
			redirectAttributes.addFlashAttribute("infos", infos);
		} else {
			List<String> errors = new ArrayList<String>();
			errors.add(messageSource.getMessage("error.vip.failure", null, locale));
			redirectAttributes.addFlashAttribute("errors", errors);
		}
		return "redirect:/admin/vip";
	}
}
