package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cncsys.imgz.entity.PhotoEntity;

public interface PhotoMapper {

	List<PhotoEntity> selectByFolder(@Param("username") String username, @Param("folder") int folder);

	List<PhotoEntity> selectByGuest(@Param("guest") String guest);

	void insertPhoto(PhotoEntity photo);

	String deletePhoto(@Param("username") String username, @Param("folder") int folder,
			@Param("thumbnail") String thumbnail);

	void deleteByFolder(@Param("username") String username, @Param("folder") int folder);

	int updatePrice(PhotoEntity photo);

	void updateByFolder(PhotoEntity photo);
}
