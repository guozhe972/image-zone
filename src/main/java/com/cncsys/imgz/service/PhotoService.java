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
	public List<PhotoEntity> getPhotosByFolder(String username, int folder) {
		List<PhotoEntity> result = photoMapper.selectByFolder(username, folder);
		return result;
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> getPhotosByGuest(String guest) {
		List<PhotoEntity> result = photoMapper.selectByGuest(guest);
		return result;
	}

	@Transactional
	public void insertPhoto(PhotoEntity photo) {
		photoMapper.insertPhoto(photo);
	}

	@Transactional
	public String deletePhoto(String username, int folder, String thumbnail) {
		String original = photoMapper.deletePhoto(username, folder, thumbnail);
		return original;
	}

	@Transactional
	public int updatePrice(PhotoEntity photo) {
		int price = photoMapper.updatePrice(photo);
		return price;
	}

	@Transactional
	public void updateAllPrice(PhotoEntity photo) {
		photoMapper.updateByFolder(photo);
	}
}
