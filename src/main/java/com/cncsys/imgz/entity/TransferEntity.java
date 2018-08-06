package com.cncsys.imgz.entity;

import org.joda.time.DateTime;

import lombok.Data;

@Data
public class TransferEntity {
	private String transno;
	private String username;
	private String bank;
	private String branch;
	private int actype;
	private String acnumber;
	private String acname;
	private int amount;
	private boolean done;
	private DateTime createdt;
	private DateTime updatedt;
}
