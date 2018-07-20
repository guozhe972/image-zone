package com.cncsys.imgz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.mapper.FolderMapper;

@Service
public class FolderService {

	@Autowired
	private FolderMapper folderMapper;

	@Transactional(readOnly = true)
	public List<FolderEntity> getUserFolderList(String username) {
		List<FolderEntity> result = folderMapper.selectFolderListByUser(username);
		return result;
	}

	@Transactional(readOnly = true)
	public FolderEntity getUserFolder(String username, int seq) {
		FolderEntity result = folderMapper.selectOneFolderByUser(username, seq);
		return result;
	}
}
