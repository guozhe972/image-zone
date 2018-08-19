package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class PhotoForm {

	public static interface Price {
	};

	private String username;
	private int folder;
	private String thumbnail;
	private int price;
	private boolean incart;
}
