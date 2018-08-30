package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class PayForm {

	private String email;
	private int amount;
	private String token;
}
