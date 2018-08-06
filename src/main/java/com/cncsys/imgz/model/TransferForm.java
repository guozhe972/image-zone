package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class TransferForm {

	private String bank;
	private String branch;
	private int actype = 1;
	private String acnumber;
	private String acname;
	private int amount;
}
