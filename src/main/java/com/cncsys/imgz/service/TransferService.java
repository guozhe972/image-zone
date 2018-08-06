package com.cncsys.imgz.service;

import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.TransferEntity;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.mapper.TransferMapper;

@Service
public class TransferService {

	@Autowired
	private TransferMapper transferMapper;

	@Autowired
	private AccountMapper accountMapper;

	private final Random intRandom = new Random();

	public String createNumber() {
		String number = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS").print(DateTime.now());
		return number + String.format("%03d", intRandom.nextInt(1000));
	}

	@Transactional(readOnly = true)
	public List<TransferEntity> selectTransfer(boolean done) {
		List<TransferEntity> result = transferMapper.selectTransfer(done);
		return result;
	}

	@Transactional
	public void acceptTransfer(TransferEntity transfer) {
		if (transferMapper.insertTransfer(transfer) > 0) {
			accountMapper.updateBalance(transfer.getUsername(), -transfer.getAmount());
		}
	}

	@Transactional
	public int updateTransfer(String transno) {
		return transferMapper.updateTransfer(transno, DateTime.now());
	}
}
