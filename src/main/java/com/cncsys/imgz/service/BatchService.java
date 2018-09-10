package com.cncsys.imgz.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.AccountEntity;
import com.cncsys.imgz.entity.FolderEntity;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.mapper.AccountMapper;
import com.cncsys.imgz.mapper.FolderMapper;
import com.cncsys.imgz.mapper.OrderMapper;
import com.cncsys.imgz.mapper.PhotoMapper;

@Service
public class BatchService {
	private static final Logger logger = LoggerFactory.getLogger(BatchService.class);

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Value("${order.file.path}")
	private String ORDER_PATH;

	@Value("${folder.expired.days}")
	private int EXPIRED_DAYS;

	//@Value("${order.download.days}")
	//private int DOWNLOAD_DAYS;

	//@Value("${transfer.history.months}")
	//private int HISTORY_MONTHS;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private FolderMapper folderMapper;

	@Autowired
	private PhotoMapper photoMapper;

	@Autowired
	private AccountMapper accountMapper;

	//@Autowired
	//private TransferMapper transferMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// 毎日の1時に起動
	@Transactional
	@Scheduled(cron = "0 0 1 * * *")
	public void initFolder() throws IOException {
		logger.info("start init expired folder batch.");
		try (Cursor<FolderEntity> folders = folderMapper.selectByExpiry(LocalDate.now());) {
			for (FolderEntity entity : folders) {
				String username = entity.getUsername();
				String seq = String.valueOf(entity.getSeq());
				logger.info("init folder: [{}], [{}]", username, seq);

				// clear db
				folderMapper.updateShared(username, entity.getSeq(), false, null, DateTime.now());
				String guest = entity.getGuest();
				AccountEntity account = new AccountEntity();
				account.setUsername(guest);
				account.setPassword(passwordEncoder.encode(guest));
				account.setEnabled(false);
				account.setExpiredt(LocalDate.now().plusDays(EXPIRED_DAYS));
				accountMapper.updateAccount(account);
				photoMapper.deleteByFolder(username, entity.getSeq());

				// clear file
				fileHelper.deleteFolder(new File(UPLOAD_PATH + "/" + username + "/" + seq));
				fileHelper.deleteFolder(new File(ORIGINAL_PATH + "/" + username + "/" + seq));
			}
		}
		logger.info("end init expired folder batch.");
	}

	@Transactional
	@Scheduled(cron = "0 0 1 * * *")
	public void delDummyOrder() {
		logger.info("start delete dummy order batch.");
		//DateTime createdt = DateTime.now().minusDays(DOWNLOAD_DAYS).withTimeAtStartOfDay();
		DateTime createdt = DateTime.now().minusDays(1).withTimeAtStartOfDay();
		logger.info("delete createdt < [{}]", createdt);

		List<String> orders = orderMapper.selectByDummy(createdt);
		for (String orderno : orders) {
			logger.info("delete order: [{}]", orderno);
			fileHelper.deleteFolder(new File(ORDER_PATH + "/" + orderno));
			//orderMapper.deleteOrder(orderno, false);
		}
		logger.info("end delete dummy order batch.");
	}

	@Transactional(readOnly = true)
	@Scheduled(cron = "0 0 1 * * *")
	public void delOrderPhoto() {
		logger.info("start delete expired photo batch.");
		LocalDate expiredt = LocalDate.now().minusDays(1);
		logger.info("delete expiredt = [{}]", expiredt);

		List<String> orders = orderMapper.selectByExpiry(expiredt);
		for (String orderno : orders) {
			logger.info("delete order: [{}]", orderno);
			fileHelper.deleteFolder(new File(ORDER_PATH + "/" + orderno));
		}
		logger.info("end delete expired photo batch.");
	}

	/*
	@Transactional
	@Scheduled(cron = "0 0 1 * * *")
	public void delOrderData() {
		logger.info("start delete expired data batch.");
		LocalDate expiredt = LocalDate.now().minusDays(1);
		logger.info("delete expiredt < [{}]", expiredt);

		List<String> orders = orderMapper.selectByBatch(expiredt);
		for (String orderno : orders) {
			logger.info("delete order: [{}]", orderno);
			orderMapper.deleteOrder(orderno, true);
		}
		logger.info("end delete expired data batch.");
	}
	*/

	/*
	// 毎月1日の1時に起動
	@Transactional
	@Scheduled(cron = "0 0 1 1 * *")
	public void delTransfer() {
		logger.info("start delete old transfer batch.");
		DateTime createdt = DateTime.now().minusMonths(HISTORY_MONTHS).withDayOfMonth(1).withTimeAtStartOfDay();
		logger.info("delete createdt < [{}]", createdt);
		transferMapper.deleteTransfer(createdt);
		logger.info("end delete old transfer batch.");
	}
	*/
}
