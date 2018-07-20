package com.cncsys.imgz.mapper;

import org.apache.ibatis.annotations.Param;

import com.cncsys.imgz.entity.AccountEntity;

public interface AccountMapper {

	AccountEntity selectUser(@Param("username") String username);

	int registerUser(AccountEntity account);
}
