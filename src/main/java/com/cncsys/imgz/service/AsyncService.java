package com.cncsys.imgz.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cncsys.imgz.helper.ImageEditor;

@Async
@Service
public class AsyncService {
	private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

	@Autowired
	private ImageEditor imageEditor;

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
}
