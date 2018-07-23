package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class PhotoForm {

	private String username;
	private int folder;
	private String thumbnail;
	private String link;
	private int price;
	private boolean shared;
}
