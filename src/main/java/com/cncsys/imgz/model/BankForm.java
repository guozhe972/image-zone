package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class BankForm {

	private String transno;
	private String bank;
	private String branch;
	/**
	 * 口座種類
	 * 1:普通、2:当座
	 */
	private int actype = 1;
	private String acnumber;
	private String acname;
	private int amount;
}
