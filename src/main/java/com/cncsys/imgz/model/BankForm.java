package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class BankForm {

	/**
	 * 口座種類
	 * 1:普通、2:当座
	 */
	public static final Integer[] AcTypes = { 1, 2 };

	private String transno;
	private String bank;
	private String branch;
	private int actype;
	private String acnumber;
	private String acname;
	private int amount;
	private boolean done;
}
