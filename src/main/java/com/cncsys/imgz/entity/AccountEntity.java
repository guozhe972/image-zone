package com.cncsys.imgz.entity;

import org.joda.time.LocalDate;

import lombok.Data;

@Data
public class AccountEntity {

	public enum Authority {
		ADMIN, USER, GUEST, NONE
	}

	private String username;
	private String password;
	private String email;
	private Authority authority;
	private LocalDate createdt;
	private LocalDate expiredt;
	private boolean enabled;
}
