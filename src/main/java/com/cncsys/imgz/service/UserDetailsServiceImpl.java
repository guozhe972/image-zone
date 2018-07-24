package com.cncsys.imgz.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.AccountEntity.Authority;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.model.LoginUser;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private AccountMapper accountMapper;

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		AccountEntity account = accountMapper.selectAccount(username);
		if (account == null) {
			throw new UsernameNotFoundException("Username not found");
		}

		Authority authority = account.getAuthority();
		List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(authority.toString()));

		boolean nonExpired = true;
		if (authority == Authority.GUEST) {
			LocalDate expiredt = account.getExpiredt();
			if (expiredt != null && LocalDate.now().isAfter(expiredt)) {
				nonExpired = false;
			}
		}

		return new LoginUser(account, nonExpired, authorities);
	}
}
