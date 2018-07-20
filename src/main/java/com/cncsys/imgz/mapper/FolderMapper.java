package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.entity.FolderEntity.Status;

public interface FolderMapper {

	List<FolderEntity> selectFolderListByUser(@Param("username") String username);

	FolderEntity selectOneFolderByUser(@Param("username") String username, @Param("seq") int seq);

	int createFolder(@Param("username") String username, @Param("status") Status status);
}
