package com.cncsys.imgz.entity;

import org.joda.time.LocalDate;

import lombok.Data;

@Data
public class FolderEntity {

	public enum Status {
		Free, Upload, Shared
	}

	private String username;
	private int seq;
	private String name;
	private String status;
	private String guest;
	private LocalDate expiredt;
}
