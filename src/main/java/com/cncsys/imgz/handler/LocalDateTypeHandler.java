package com.cncsys.imgz.handler;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class LocalDateTypeHandler extends BaseTypeHandler<LocalDate> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setDate(i, new Date(parameter.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis()));
	}

	@Override
	public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Date date = rs.getDate(columnName);
		if (date == null) {
			return null;
		} else {
			return new LocalDate(date.getTime());
		}
	}

	@Override
	public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Date date = rs.getDate(columnIndex);
		if (date == null) {
			return null;
		} else {
			return new LocalDate(date.getTime());
		}
	}

	@Override
	public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Date date = cs.getDate(columnIndex);
		if (date == null) {
			return null;
		} else {
			return new LocalDate(date.getTime());
		}
	}
}
