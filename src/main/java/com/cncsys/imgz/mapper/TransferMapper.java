package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

import com.cncsys.imgz.entity.TransferEntity;

public interface TransferMapper {

	List<TransferEntity> selectTransfer(@Param("done") boolean done);

	int insertTransfer(TransferEntity transfer);

	int updateTransfer(@Param("transno") String transno, @Param("updatedt") DateTime updatedt);
}
