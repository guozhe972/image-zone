package com.cncsys.imgz.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.helper.ImageEditor;
import com.cncsys.imgz.mapper.PhotoMapper;
import com.cncsys.imgz.model.LoginUser;

@Async
@Service
public class AsyncService {

	private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Autowired
	private ImageEditor imageEditor;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private PhotoMapper photoMapper;

	@Transactional
	public void upload(LoginUser user, int folder, List<String> files) {
		logger.info("async start.");

		String thumbPath = UPLOAD_PATH + "/" +
				user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.createDirectory(thumbPath);

		String originPath = ORIGINAL_PATH + "/" +
				user.getUsername() + "/" + String.valueOf(folder);
		fileHelper.createDirectory(originPath);

		List<PhotoEntity> lstPhoto = new ArrayList<PhotoEntity>();
		byte[] buffer = new byte[1024];
		for (String item : files) {
			File file = new File(item);
			if (file.getName().toLowerCase().endsWith(".zip")) {
				try {
					ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
					ZipEntry entry = zis.getNextEntry();
					while (entry != null) {
						if (!entry.isDirectory() && entry.getName().toLowerCase().matches(".+(\\.jpg|\\.jpeg)$")) {
							String ext = fileHelper.getExtension(entry.getName());

							// unzip original image
							String originId = UUID.randomUUID().toString() + ext;
							File original = new File(originPath + "/" + originId);
							FileOutputStream fos = new FileOutputStream(original);
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
							fos.close();

							// make thumbnail image
							String thumbId = UUID.randomUUID().toString() + ext;
							File preview = new File(thumbPath + "/" + "preview_" + thumbId);
							File thumbnail = new File(thumbPath + "/" + "thumbnail_" + thumbId);
							BufferedImage marked = imageEditor.getPreview(ImageIO.read(original));
							ImageIO.write(marked, "jpeg", preview);
							BufferedImage scaled = imageEditor.getThumbnail(marked);
							ImageIO.write(scaled, "jpeg", thumbnail);

							// add to entity
							PhotoEntity photo = new PhotoEntity();
							photo.setUsername(user.getUsername());
							photo.setFolder(folder);
							photo.setThumbnail(thumbId);
							photo.setOriginal(originId);
							photo.setPrice(0);
							photo.setShared(false);
							photo.setCreatedt(LocalDate.now());
							lstPhoto.add(photo);
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
				String ext = fileHelper.getExtension(file.getName());
				try {
					// move original image
					String originId = UUID.randomUUID().toString() + ext;
					File original = new File(originPath + "/" + originId);
					file.renameTo(original);

					// make thumbnail image
					String thumbId = UUID.randomUUID().toString() + ext;
					File preview = new File(thumbPath + "/" + "preview_" + thumbId);
					File thumbnail = new File(thumbPath + "/" + "thumbnail_" + thumbId);
					BufferedImage marked = imageEditor.getPreview(ImageIO.read(original));
					ImageIO.write(marked, "jpeg", preview);
					BufferedImage scaled = imageEditor.getThumbnail(marked);
					ImageIO.write(scaled, "jpeg", thumbnail);

					// add to entity
					PhotoEntity photo = new PhotoEntity();
					photo.setUsername(user.getUsername());
					photo.setFolder(folder);
					photo.setThumbnail(thumbId);
					photo.setOriginal(originId);
					photo.setPrice(0);
					photo.setShared(false);
					photo.setCreatedt(LocalDate.now());
					lstPhoto.add(photo);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// insert to db
		photoMapper.insertPhotos(lstPhoto);

		logger.info("async end.");
	}
}
