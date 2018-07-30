package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class ChargeForm {


	public static interface Input {
	};

	private String email;
	private String number;
	private String expiry;
	private String name;
	private String code;
	private String token;
}
