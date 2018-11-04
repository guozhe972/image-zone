package com.cncsys.imgz.service;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.AccountEntity.Authority;
import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.mapper.FolderMapper;
import com.cncsys.imgz.mapper.PhotoMapper;

@Service
public class FolderService {

	@Value("${folder.expired.days}")
	private int EXPIRED_DAYS;

	@Autowired
	private FolderMapper folderMapper;

	@Autowired
	private PhotoMapper photoMapper;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<FolderEntity> getUserFolders(String username) {
		List<FolderEntity> result = folderMapper.selectByUser(username);
		return result;
	}

	@Transactional(readOnly = true)
	public List<FolderEntity> getSharedFolders(String username) {
		List<FolderEntity> result = folderMapper.selectByShared(username);
		return result;
	}

	@Transactional(readOnly = true)
	public FolderEntity getUserFolder(String username, int seq) {
		FolderEntity folder = folderMapper.selectFolder(username, seq);
		return folder;
	}

	@Transactional(readOnly = true)
	public int getFolderCount(String username) {
		return folderMapper.selectCount(username);
	}

	@Transactional
	public void createFolder(String username, String foldername, boolean vip) {
		int seq = folderMapper.insertFolder(username, foldername, DateTime.now());
		String guest = username + "." + String.format("%02d", seq);

		AccountEntity account = new AccountEntity();
		account.setUsername(guest);
		account.setPassword(passwordEncoder.encode(guest));
		account.setEmail(null);
		account.setAuthority(Authority.GUEST);
		account.setCreatedt(LocalDate.now().toDateTimeAtStartOfDay());
		account.setEnabled(false);
		account.setVip(vip);
		account.setExpiredt(LocalDate.now().plusDays(EXPIRED_DAYS / 2));
		if (accountMapper.insertAccount(account) > 0) {
			folderMapper.updateGuest(username, seq, guest);
		}
	}

	@Transactional
	public void shareFolder(String username, int seq, String password,
			LocalDate plansdt, LocalDate expiredt) {
		folderMapper.shareFolder(username, seq, password, DateTime.now());
		String guest = username + "." + String.format("%02d", seq);

		AccountEntity account = new AccountEntity();
		account.setUsername(guest);
		if (password == null || password.isEmpty()) {
			account.setPassword("");
		} else {
			account.setPassword(passwordEncoder.encode(password));
		}
		account.setEnabled(true);
		account.setCreatedt(plansdt.toDateTimeAtStartOfDay());
		account.setExpiredt(expiredt);
		accountMapper.updateAccount(account);
	}

	@Transactional
	public void initFolder(String username, int seq) {
		folderMapper.initFolder(username, seq, DateTime.now());
		String guest = username + "." + String.format("%02d", seq);

		AccountEntity account = new AccountEntity();
		account.setUsername(guest);
		account.setPassword(passwordEncoder.encode(guest));
		account.setEnabled(false);
		account.setCreatedt(LocalDate.now().toDateTimeAtStartOfDay());
		account.setExpiredt(LocalDate.now().plusDays(EXPIRED_DAYS / 2));
		accountMapper.updateAccount(account);

		photoMapper.deleteByFolder(username, seq);
	}

	@Transactional
	public String changeName(String username, int seq, String name) {
		return folderMapper.updateFolder(username, seq, name);
	}

	@Transactional(readOnly = true)
	public boolean isLocked(String username, int seq) {
		FolderEntity folder = folderMapper.selectFolder(username, seq);
		return folder.isLocked();
	}

	@Transactional
	public void lock(String username, int seq) {
		folderMapper.updateLocked(username, seq, true);
	}

	@Transactional
	public void unlock(String username, int seq) {
		folderMapper.updateLocked(username, seq, false);
	}
}
