package com.cncsys.imgz.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SignupForm implements Serializable {
	private static final long serialVersionUID = 1L;

	public static interface Input {
	};

	public static interface Confirm {
	};

	private String username;
	private String email;
	private String password;
	private String confirm;
	private String code;
	private String token;
}
