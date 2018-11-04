package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.cncsys.imgz.entity.FolderEntity;

public interface FolderMapper {

	List<FolderEntity> selectByUser(@Param("username") String username);

	List<FolderEntity> selectByShared(@Param("username") String username);

	Cursor<FolderEntity> selectByExpiry(@Param("expiredt") LocalDate expiredt);

	FolderEntity selectFolder(@Param("username") String username, @Param("seq") int seq);

	int selectCount(@Param("username") String username);

	int insertFolder(@Param("username") String username, @Param("name") String name,
			@Param("createdt") DateTime createdt);

	void updateGuest(@Param("username") String username, @Param("seq") int seq, @Param("guest") String guest);

	int updateLocked(@Param("username") String username, @Param("seq") int seq, @Param("locked") boolean locked);

	int shareFolder(@Param("username") String username, @Param("seq") int seq, @Param("cipher") String cipher,
			@Param("createdt") DateTime createdt);

	int initFolder(@Param("username") String username, @Param("seq") int seq, @Param("createdt") DateTime createdt);

	String updateFolder(@Param("username") String username, @Param("seq") int seq, @Param("name") String name);
}
