package com.cncsys.imgz.entity;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import lombok.Data;

@Data
public class OrderEntity {
	private String orderno;
	private String email;
	private String username;
	private int folder;
	private String thumbnail;
	private String original;
	private String filename;
	private int price;
	private DateTime createdt;
	private boolean charged;
	private LocalDate expiredt;
	private int qty;
	private int amount;
}
