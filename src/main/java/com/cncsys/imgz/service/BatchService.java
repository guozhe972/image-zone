package com.cncsys.imgz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

	private static final Logger logger = LoggerFactory.getLogger(BatchService.class);

	// 毎日の1時に起動
	@Scheduled(cron = "0 0 1 * * *")
	public void clean() {
		logger.info("batch start.");
		// TODO: init expired folder
		logger.info("batch end.");
	}
}
