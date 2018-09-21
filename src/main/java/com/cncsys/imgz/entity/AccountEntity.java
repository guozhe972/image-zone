package com.cncsys.imgz.entity;

import java.math.BigDecimal;

import org.joda.time.DateTime;
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
	private BigDecimal balance;
	private DateTime createdt;
	private boolean enabled;
	private boolean vip;
	private LocalDate expiredt;
	private DateTime logindt;
}
