package com.cncsys.imgz.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.mapper.FolderMapper;
import com.cncsys.imgz.mapper.PhotoMapper;

@Service
public class FolderService {

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
	public FolderEntity getUserFolder(String username, int seq) {
		FolderEntity folder = folderMapper.selectFolder(username, seq);
		return folder;
	}

	@Transactional
	public void shareFolder(String username, int seq, String password, LocalDate expiredt) {
		String guest = folderMapper.updateShared(username, seq, true);

		AccountEntity account = new AccountEntity();
		account.setUsername(guest);
		account.setPassword(passwordEncoder.encode(password));
		account.setEnabled(true);
		account.setExpiredt(expiredt);
		accountMapper.updateAccount(account);
	}

	@Transactional
	public void initFolder(String username, int seq) {
		String guest = folderMapper.updateShared(username, seq, false);

		AccountEntity account = new AccountEntity();
		account.setUsername(guest);
		account.setPassword(passwordEncoder.encode(guest));
		account.setEnabled(false);
		account.setExpiredt(null);
		accountMapper.updateAccount(account);

		photoMapper.deleteByFolder(username, seq);
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
