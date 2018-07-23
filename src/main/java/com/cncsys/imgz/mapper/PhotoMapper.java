package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cncsys.imgz.entity.PhotoEntity;

public interface PhotoMapper {

	List<PhotoEntity> selectPhotoByUser(@Param("username") String username, @Param("folder") int folder);

	int insertPhotos(List<PhotoEntity> photos);

	String deletePhoto(@Param("username") String username, @Param("folder") int folder,
			@Param("thumbnail") String thumbnail);

	void clearFolder(@Param("username") String username, @Param("folder") int folder);

	int updatePrice(PhotoEntity photo);

	int updateShared(PhotoEntity photo);
}
