package com.cncsys.imgz.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.helper.ImageEditor;
import com.cncsys.imgz.mapper.OrderMapper;

@Async
@Service
public class AsyncService {
	private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

	@Value("${order.file.path}")
	private String ORDER_PATH;

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private ImageEditor imageEditor;

	@Autowired
	private FileHelper fileHelper;

	public void makeThumbnail(List<String> photos) {
		logger.info("async start.");
		for (String photo : photos) {
			try {
				File original = new File(photo);
				File thumbnail = new File(original.getParent() + "/" + "thumbnail_" + original.getName());
				BufferedImage scaled = imageEditor.getThumbnail(ImageIO.read(original));
				ImageIO.write(scaled, "jpeg", thumbnail);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("async end.");
	}

	@Transactional
	public void deleteOrder(String orderno) {
		logger.info("async delete order start.");
		try {
			logger.info("delete " + orderno);
			fileHelper.deleteFolder(new File(ORDER_PATH + "/" + orderno));
			orderMapper.deleteOrder(orderno);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("async delete order end.");
	}
}
