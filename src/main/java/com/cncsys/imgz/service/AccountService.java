package com.cncsys.imgz.service;

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

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private FolderMapper folderMapper;

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
		AccountEntity account = new AccountEntity();
		account.setUsername(username);
		account.setPassword(passwordEncoder.encode(password));
		account.setEmail(email);
		account.setAuthority(Authority.USER);
		account.setCreatedt(LocalDate.now());
		account.setEnabled(true);
		if (accountMapper.insertAccount(account) > 0) {
			for (int i = 0; i < FOLDER_COUNT; i++) {
				int seq = folderMapper.insertFolder(username);
				String guest = username + "." + String.format("%02d", seq);
				account = new AccountEntity();
				account.setUsername(guest);
				account.setPassword(passwordEncoder.encode(guest));
				account.setEmail(null);
				account.setAuthority(Authority.GUEST);
				account.setCreatedt(LocalDate.now());
				account.setEnabled(false);
				if (accountMapper.insertAccount(account) > 0) {
					folderMapper.updateGuest(username, seq, guest);
				}
			}
		}
	}

	@Transactional
	public void updateLogindt(String username) {
		accountMapper.updateLogindt(username, LocalDate.now());
	}
}
