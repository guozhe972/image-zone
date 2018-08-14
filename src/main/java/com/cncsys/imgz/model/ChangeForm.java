package com.cncsys.imgz.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ChangeForm implements Serializable {
	private static final long serialVersionUID = 1L;

	public static interface Input {
	};

	private String username;
	private String email;
	private String password;
	private String confirm;
}
