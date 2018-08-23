package com.cncsys.imgz.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.AccountEntity.Authority;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.mapper.FolderMapper;

@Service
public class AccountService {

	@Value("${init.folder.count}")
	private int FOLDER_COUNT;

	@Value("${default.expired.days}")
	private int DEFAULT_EXPIRED;

	@Value("${cost.settle.percent}")
	private int COST_SETTLE;

	@Value("${admin.username}")
	private String ADMIN_NAME;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private FolderMapper folderMapper;

	@Transactional(readOnly = true)
	public AccountEntity getAccountInfo(String username) {
		return accountMapper.selectAccount(username);
	}

	@Transactional(readOnly = true)
	public AccountEntity getForgotAccount(String username, String password) {
		return accountMapper.selectByReset(username, password);
	}

	@Transactional(readOnly = true)
	public boolean isExistUser(String username) {
		boolean result = false;
		AccountEntity account = accountMapper.selectAccount(username);
		if (account != null) {
			result = true;
		}
		return result;
	}

	@Transactional
	public void registerUser(String username, String password, String email) {
		DateTime sysnow = DateTime.now();
		AccountEntity account = new AccountEntity();
		account.setUsername(username);
		account.setPassword(passwordEncoder.encode(password));
		account.setEmail(email);
		account.setAuthority(Authority.USER);
		account.setCreatedt(sysnow);
		account.setEnabled(true);
		if (accountMapper.insertAccount(account) > 0) {
			LocalDate expiredt = LocalDate.now().plusDays(DEFAULT_EXPIRED);
			for (int i = 0; i < FOLDER_COUNT; i++) {
				int seq = folderMapper.insertFolder(username, sysnow);
				String guest = username + "." + String.format("%02d", seq);
				account = new AccountEntity();
				account.setUsername(guest);
				account.setPassword(passwordEncoder.encode(guest));
				account.setEmail(null);
				account.setAuthority(Authority.GUEST);
				account.setCreatedt(sysnow);
				account.setEnabled(false);
				account.setExpiredt(expiredt);
				if (accountMapper.insertAccount(account) > 0) {
					folderMapper.updateGuest(username, seq, guest);
				}
			}
		}
	}

	@Transactional
	public void updateBalance(String username, int amount, int real) {
		int fee = Math.round(amount * COST_SETTLE / 100F);
		accountMapper.updateBalance(username, amount - fee);
		accountMapper.updateBalance(ADMIN_NAME, fee - (amount - real));
	}

	@Transactional
	public String changePassword(String username, String password) {
		return accountMapper.updatePassword(username, passwordEncoder.encode(password));
	}

	@Transactional
	public void updateLogindt(String username) {
		accountMapper.updateLogindt(username, DateTime.now());
	}

	@Transactional(readOnly = true)
	public LocalDate getExpiredt(String username) {
		AccountEntity account = accountMapper.selectAccount(username);
		return account.getExpiredt();
	}
}
