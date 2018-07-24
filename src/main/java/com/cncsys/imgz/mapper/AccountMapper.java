package com.cncsys.imgz.mapper;

import org.apache.ibatis.annotations.Param;
import org.joda.time.LocalDate;

import com.cncsys.imgz.entity.AccountEntity;

public interface AccountMapper {

	AccountEntity selectAccount(@Param("username") String username);

	int insertAccount(AccountEntity account);

	void updateLogindt(@Param("username") String username, @Param("logindt") LocalDate logindt);

	int updateAccount(AccountEntity account);
}
