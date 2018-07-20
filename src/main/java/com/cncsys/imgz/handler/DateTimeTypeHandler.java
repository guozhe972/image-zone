package com.cncsys.imgz.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;

public class DateTimeTypeHandler extends BaseTypeHandler<DateTime> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, DateTime parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setTimestamp(i, new Timestamp(parameter.getMillis()));
	}

	@Override
	public DateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Timestamp timestamp = rs.getTimestamp(columnName);
		if (timestamp == null) {
			return null;
		} else {
			return new DateTime(timestamp.getTime());
		}
	}

	@Override
	public DateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Timestamp timestamp = rs.getTimestamp(columnIndex);
		if (timestamp == null) {
			return null;
		} else {
			return new DateTime(timestamp.getTime());
		}
	}

	@Override
	public DateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Timestamp timestamp = cs.getTimestamp(columnIndex);
		if (timestamp == null) {
			return null;
		} else {
			return new DateTime(timestamp.getTime());
		}
	}
}
