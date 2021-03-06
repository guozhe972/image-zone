package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

import com.cncsys.imgz.entity.TransferEntity;

public interface TransferMapper {

	List<TransferEntity> selectTransfer(@Param("done") boolean done);

	List<TransferEntity> selectByUser(@Param("username") String username, @Param("createdt") DateTime createdt);

	int insertTransfer(TransferEntity transfer);

	int updateTransfer(@Param("transno") String transno, @Param("fee") int fee, @Param("updatedt") DateTime updatedt);

	void deleteTransfer(@Param("createdt") DateTime createdt);
}
