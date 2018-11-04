package com.cncsys.imgz.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.cncsys.imgz.entity.AccountEntity;

public interface AccountMapper {

	AccountEntity selectAccount(@Param("username") String username);

	AccountEntity selectByReset(@Param("username") String username, @Param("password") String password);

	int insertAccount(AccountEntity account);

	void updateLogindt(@Param("username") String username, @Param("logindt") DateTime logindt);

	int updateAccount(AccountEntity account);

	int updateVip(@Param("username") String username);

	String updateExpiredt(@Param("username") String username, @Param("expiredt") LocalDate expiredt);

	String updatePassword(@Param("username") String username, @Param("password") String password);

	BigDecimal updateBalance(@Param("username") String username, @Param("balance") BigDecimal balance);
}
