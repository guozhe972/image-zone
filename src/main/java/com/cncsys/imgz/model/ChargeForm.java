package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class ChargeForm {


	public static interface Input {
	};


	private String email;
	private String name;
	private String number;
	private String expiry;
	private String code;
	private String token;
}
