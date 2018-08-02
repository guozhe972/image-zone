package com.cncsys.imgz.model;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.AccountEntity.Authority;

public class LoginUser extends User {
	private final String email;
	private final Authority authority;
	private int balance;
	private final LocalDate expiredt;

	public LoginUser(AccountEntity account, boolean nonExpired,
			Collection<? extends GrantedAuthority> authorities) {
		super(account.getUsername(), account.getPassword(), account.isEnabled(), nonExpired, true, true,
				authorities);
		this.email = account.getEmail();
		this.authority = account.getAuthority();
		this.balance = account.getBalance();
		this.expiredt = account.getExpiredt();
	}

	public String getEmail() {
		return email;
	}

	public Authority getAuthority() {
		return authority;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public LocalDate getExpiredt() {
		return expiredt;
	}
}