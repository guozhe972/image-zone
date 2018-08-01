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
	public List<OrderEntity> getValidOrder(String number, String email) {
		List<OrderEntity> result = orderMapper.selectOrder(number, email, LocalDate.now());
		return result;
	}

	@Transactional
	public int insertOrder(OrderEntity order) {
		return orderMapper.insertOrder(order);
	}

	@Transactional
	public int chargeOrder(String number, String email) {
		return orderMapper.updateCharged(number, email, true);
	}
}
