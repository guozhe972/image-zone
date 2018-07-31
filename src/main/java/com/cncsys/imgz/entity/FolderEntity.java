package com.cncsys.imgz.entity;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import lombok.Data;

@Data
public class FolderEntity {

	private String username;
	private int seq;
	private String name;
	private boolean locked;
	private boolean shared;
	private String guest;
	private DateTime createdt;
	private LocalDate expiredt;
}
