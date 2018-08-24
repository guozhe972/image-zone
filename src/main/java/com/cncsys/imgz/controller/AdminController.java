package com.cncsys.imgz.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.TransferEntity;
import com.cncsys.imgz.model.BankForm;
import com.cncsys.imgz.model.LoginUser;
import com.cncsys.imgz.service.AccountService;
import com.cncsys.imgz.service.TransferService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AccountService accountService;

	@Autowired
	private TransferService transferService;

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
		transferService.doneTransfer(transno, fee);
		return "redirect:/admin/home";
	}
}
