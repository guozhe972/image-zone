package com.cncsys.imgz.entity;

import lombok.Data;

@Data
public class FolderEntity {

	private String username;
	private int seq;
	private String name;
	private boolean locked;
	private boolean shared;
	private String guest;
}
