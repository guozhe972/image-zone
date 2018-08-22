package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class ChangeForm {

	public static interface Input {
	};

	private String username;
	private String email;
	private String password;
	private String confirm;
}
