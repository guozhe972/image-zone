package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class OrderForm {

	private String orderno;
	private String username;
	private int folder;
	private String thumbnail;
	private int price;
	private int qty;
	private int amount;
	private String email;
	private boolean charged;
}
