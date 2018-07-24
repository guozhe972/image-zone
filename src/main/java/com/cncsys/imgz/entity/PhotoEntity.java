package com.cncsys.imgz.entity;

import org.joda.time.LocalDate;

import lombok.Data;

@Data
public class PhotoEntity {
	private String username;
	private int folder;
	private String thumbnail;
	private String original;
	private int price;
	private LocalDate createdt;
}
