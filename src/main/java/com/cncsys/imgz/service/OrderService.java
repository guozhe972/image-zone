package com.cncsys.imgz.service;

import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

	private final Random intRandom = new Random();

	public String createNumber() {
		String number = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS").print(DateTime.now());
		return number + String.format("%03d", intRandom.nextInt(1000));
	}
}
