package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cncsys.imgz.entity.FolderEntity;

public interface FolderMapper {

	List<FolderEntity> selectByUser(@Param("username") String username);

	FolderEntity selectFolder(@Param("username") String username, @Param("seq") int seq);

	int insertFolder(@Param("username") String username);

	void updateGuest(@Param("username") String username, @Param("seq") int seq, @Param("guest") String guest);

	int updateLocked(@Param("username") String username, @Param("seq") int seq, @Param("locked") boolean locked);

	String updateShared(@Param("username") String username, @Param("seq") int seq, @Param("shared") boolean shared);
}
