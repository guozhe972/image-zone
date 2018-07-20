package com.cncsys.imgz.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cncsys.imgz.helper.ImageEditor;
import com.cncsys.imgz.model.LoginUser;

@Async
@Service
public class AsyncService {

	private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Autowired
	private ImageEditor imageEditor;

	public void upload(LoginUser user, int folder, List<String> files) {
		logger.info("async start.");

		// create folder
		File uploadPath = new File(
				UPLOAD_PATH + File.separator + user.getUsername() + File.separator + String.valueOf(folder));
		if (!uploadPath.exists()) {
			uploadPath.mkdirs();
		}

		int idx = 1;
		String uuid = UUID.randomUUID().toString();
		byte[] buffer = new byte[1024];
		for (String item : files) {
			File file = new File(item);
			if (file.getName().toLowerCase().endsWith(".zip")) {
				String prefix = file.getParent() + File.separator
						+ file.getName().substring(0, file.getName().lastIndexOf("."));
				try {
					ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
					ZipEntry entry = zis.getNextEntry();
					int cnt = 1;
					while (entry != null) {
						if (!entry.isDirectory() && entry.getName().toLowerCase().matches(".+(\\.jpg|\\.jpeg)$")) {
							String ext = entry.getName().substring(entry.getName().lastIndexOf("."));
							File unzip = new File(prefix + String.format("-%08d%s", cnt++, ext));
							FileOutputStream fos = new FileOutputStream(unzip);
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
							fos.close();

							String suffix = uuid + String.format("_%08d%s", idx++, ext);
							File original = new File(uploadPath + File.separator + "original_" + suffix);
							unzip.renameTo(original);

							BufferedImage source = ImageIO.read(original);
							BufferedImage marked = imageEditor.getPreview(source);
							BufferedImage scaled = imageEditor.getThumbnail(marked);

							File preview = new File(uploadPath + File.separator + "preview_" + suffix);
							ImageIO.write(marked, "jpeg", preview);

							File thumbnail = new File(uploadPath + File.separator + "thumbnail_" + suffix);
							ImageIO.write(scaled, "jpeg", thumbnail);
						}
						entry = zis.getNextEntry();
					}
					zis.closeEntry();
					zis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				file.delete();
			} else {
				String ext = file.getName().substring(file.getName().lastIndexOf("."));
				String suffix = uuid + String.format("_%08d%s", idx++, ext);
				try {
					File original = new File(uploadPath + File.separator + "original_" + suffix);
					file.renameTo(original);

					BufferedImage source = ImageIO.read(original);
					BufferedImage marked = imageEditor.getPreview(source);
					BufferedImage scaled = imageEditor.getThumbnail(marked);

					File preview = new File(uploadPath + File.separator + "preview_" + suffix);
					ImageIO.write(marked, "jpeg", preview);

					File thumbnail = new File(uploadPath + File.separator + "thumbnail_" + suffix);
					ImageIO.write(scaled, "jpeg", thumbnail);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("async end.");
	}
}
