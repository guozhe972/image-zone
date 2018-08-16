package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.joda.time.LocalDate;

import com.cncsys.imgz.entity.PhotoEntity;

public interface PhotoMapper {

	List<PhotoEntity> selectByFolder(@Param("username") String username, @Param("folder") int folder);

	List<PhotoEntity> selectByGuest(@Param("guest") String guest);

	List<PhotoEntity> selectByPublic(@Param("expiredt") LocalDate expiredt);

	PhotoEntity selectPhoto(@Param("username") String username, @Param("folder") int folder,
			@Param("thumbnail") String thumbnail);

	void insertPhoto(PhotoEntity photo);

	String deletePhoto(@Param("username") String username, @Param("folder") int folder,
			@Param("thumbnail") String thumbnail);

	void deleteByFolder(@Param("username") String username, @Param("folder") int folder);

	int updatePrice(PhotoEntity photo);

	void updateByFolder(PhotoEntity photo);
}
