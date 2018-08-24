package com.cncsys.imgz.service;

import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.cncsys.imgz.entity.TransferEntity;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.mapper.TransferMapper;

@Service
public class TransferService {

	@Value("${cost.transfer.fee}")
	private int COST_TRANSFER;

	@Value("${admin.username}")
	private String ADMIN_NAME;

	@Value("${default.history.months}")
	private int HISTORY_MONTHS;

	@Autowired
	private TransferMapper transferMapper;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final Random intRandom = new Random();

	public String createNumber() {
		String number = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS").print(DateTime.now());
		return number + String.format("%03d", intRandom.nextInt(1000));
	}

	@Transactional(readOnly = true)
	public List<TransferEntity> getRequest(boolean done) {
		List<TransferEntity> result = transferMapper.selectTransfer(done);
		return result;
	}

	@Transactional(readOnly = true)
	public List<TransferEntity> getHistory(String username) {
		DateTime createdt = DateTime.now().minusMonths(HISTORY_MONTHS).withDayOfMonth(1).withTimeAtStartOfDay();
		List<TransferEntity> result = transferMapper.selectByUser(username, createdt);
		return result;
	}

	public int acceptTransfer(TransferEntity transfer) {
		int balance = -1;
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

		try {
			balance = accountMapper.updateBalance(transfer.getUsername(), -Math.abs(transfer.getAmount()));
			if (balance < 0) {
				transactionManager.rollback(status);
			} else {
				transfer.setAmount(transfer.getAmount() - COST_TRANSFER);
				transferMapper.insertTransfer(transfer);
				//accountMapper.updateBalance(ADMIN_NAME, COST_TRANSFER);
				transactionManager.commit(status);
			}
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}

		return balance;
	}

	@Transactional
	public void doneTransfer(String transno, int fee) {
		transferMapper.updateTransfer(transno, DateTime.now());
		//accountMapper.updateBalance(ADMIN_NAME, -Math.abs(fee));
		accountMapper.updateBalance(ADMIN_NAME, COST_TRANSFER - Math.abs(fee));
	}
}
