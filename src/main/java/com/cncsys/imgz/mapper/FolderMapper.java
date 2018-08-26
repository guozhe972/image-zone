package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

import com.cncsys.imgz.entity.FolderEntity;

public interface FolderMapper {

	List<FolderEntity> selectByUser(@Param("username") String username);

	FolderEntity selectFolder(@Param("username") String username, @Param("seq") int seq);

	int selectCount(@Param("username") String username);

	int insertFolder(@Param("username") String username, @Param("name") String name,
			@Param("createdt") DateTime createdt);

	void updateGuest(@Param("username") String username, @Param("seq") int seq, @Param("guest") String guest);

	int updateLocked(@Param("username") String username, @Param("seq") int seq, @Param("locked") boolean locked);

	int updateShared(@Param("username") String username, @Param("seq") int seq,
			@Param("shared") boolean shared, @Param("cipher") String cipher);

	String updateFolder(@Param("username") String username, @Param("seq") int seq, @Param("name") String name);
}
