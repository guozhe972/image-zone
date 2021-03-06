package com.cncsys.imgz.service;

import java.math.BigDecimal;

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

	@Value("${folder.count.init}")
	private int FOLDER_COUNT;

	@Value("${folder.expired.days}")
	private int EXPIRED_DAYS;

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
		AccountEntity account;
		if (password == null || password.isEmpty()) {
			account = null;
		} else {
			account = accountMapper.selectByReset(username, password);
		}
		return account;
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
		account.setVip(false);
		if (accountMapper.insertAccount(account) > 0) {
			DateTime createdt = LocalDate.now().toDateTimeAtStartOfDay();
			LocalDate expiredt = LocalDate.now().plusDays(EXPIRED_DAYS / 2);
			for (int i = 0; i < FOLDER_COUNT; i++) {
				int seq = folderMapper.insertFolder(username, "", sysnow);
				String guest = username + "." + String.format("%02d", seq);
				account = new AccountEntity();
				account.setUsername(guest);
				account.setPassword(passwordEncoder.encode(guest));
				account.setEmail(null);
				account.setAuthority(Authority.GUEST);
				account.setCreatedt(createdt);
				account.setEnabled(false);
				account.setVip(false);
				account.setExpiredt(expiredt);
				if (accountMapper.insertAccount(account) > 0) {
					folderMapper.updateGuest(username, seq, guest);
				}
			}
		}
	}

	@Transactional
	public void updateBalance(String username, int amount, BigDecimal real) {
		BigDecimal fee = BigDecimal.valueOf(amount * COST_SETTLE).divide(BigDecimal.valueOf(100), 2,
				BigDecimal.ROUND_DOWN);
		accountMapper.updateBalance(username, BigDecimal.valueOf(amount).subtract(fee));
		accountMapper.updateBalance(ADMIN_NAME, fee.subtract(BigDecimal.valueOf(amount).subtract(real)));
	}

	@Transactional
	public String changePassword(String username, String password) {
		return accountMapper.updatePassword(username, passwordEncoder.encode(password));
	}

	@Transactional
	public void updateLogindt(String username) {
		accountMapper.updateLogindt(username, DateTime.now());
	}

	@Transactional
	public String updateVip(String username, LocalDate expiredt) {
		int result = accountMapper.updateVip(username);
		if (result > 0) {
			return accountMapper.updateExpiredt(username, expiredt);
		} else {
			return null;
		}
	}
}
