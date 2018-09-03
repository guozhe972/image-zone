package com.cncsys.imgz.service;

import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.OrderEntity;
import com.cncsys.imgz.mapper.OrderMapper;

@Service
public class OrderService {

	@Autowired
	private OrderMapper orderMapper;

	private final Random intRandom = new Random();

	public String createNumber() {
		String number = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS").print(DateTime.now());
		return number + String.format("%03d", intRandom.nextInt(1000));
	}

	@Transactional(readOnly = true)
	public List<OrderEntity> getValidOrder(String orderno, String email) {
		List<OrderEntity> result = orderMapper.selectOrder(orderno, email, LocalDate.now());
		return result;
	}

	@Transactional(readOnly = true)
	public List<OrderEntity> getOrderByFolder(String username, int folder) {
		List<OrderEntity> result = orderMapper.selectByFolder(username, folder);
		return result;
	}

	@Transactional
	public int insertOrder(OrderEntity order) {
		return orderMapper.insertOrder(order);
	}

	@Transactional
	public int updateOrder(String orderno, String email, LocalDate expiredt) {
		return orderMapper.updateCharged(orderno, email, expiredt);
	}
}
