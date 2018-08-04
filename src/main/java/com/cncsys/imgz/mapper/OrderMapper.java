package com.cncsys.imgz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.joda.time.LocalDate;

import com.cncsys.imgz.entity.OrderEntity;

public interface OrderMapper {

	List<OrderEntity> selectOrder(@Param("number") String number, @Param("email") String email,
			@Param("expiredt") LocalDate expiredt);

	List<OrderEntity> selectByFolder(@Param("username") String username, @Param("folder") int folder);

	int insertOrder(OrderEntity order);

	int updateCharged(@Param("number") String number, @Param("email") String email, @Param("charged") boolean charged);
}
