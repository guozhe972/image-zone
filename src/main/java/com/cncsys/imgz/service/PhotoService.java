package com.cncsys.imgz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.mapper.PhotoMapper;

@Service
public class PhotoService {

	@Autowired
	private PhotoMapper photoMapper;

	@Transactional(readOnly = true)
	public List<PhotoEntity> selectPhotoByUser(String username, int folder) {
		List<PhotoEntity> result = photoMapper.selectPhotoByUser(username, folder);
		return result;
	}

	@Transactional
	public String deletePhoto(String username, int folder, String thumbnail) {
		String original = photoMapper.deletePhoto(username, folder, thumbnail);
		return original;
	}

	@Transactional
	public void clearFolder(String username, int folder) {
		photoMapper.clearFolder(username, folder);
	}

	@Transactional
	public int updatePrice(PhotoEntity photo) {
		int price = photoMapper.updatePrice(photo);
		return price;
	}

	@Transactional
	public int updateShared(PhotoEntity photo) {
		int result = photoMapper.updateShared(photo);
		return result;
	}
}
