package com.cncsys.imgz.entity;

import org.joda.time.DateTime;

import lombok.Data;

@Data
public class PhotoEntity {
	private String username;
	private int folder;
	private String thumbnail;
	private String original;
	private String filename;
	private int price;
	private DateTime createdt;
}
