package com.cncsys.imgz.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.jcodec.api.FrameGrab;
import org.jcodec.scale.AWTUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.cncsys.imgz.entity.PhotoEntity;
import com.cncsys.imgz.helper.FileHelper;
import com.cncsys.imgz.helper.ImageEditor;

@Service
public class UploadService {
	private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

	@Value("${upload.file.path}")
	private String UPLOAD_PATH;

	@Value("${upload.file.original}")
	private String ORIGINAL_PATH;

	@Autowired
	private ImageEditor imageEditor;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private PhotoService photoService;

	public void upload(String username, int folder, List<String> files, int price) {
		String charset = "UTF-8";
		Locale locale = LocaleContextHolder.getLocale();
		if ("ja".equals(locale.getLanguage())) {
			charset = "MS932";
		} else if ("zh".equals(locale.getLanguage())) {
			charset = "GBK";
		}

		String thumbPath = UPLOAD_PATH + "/" + username + "/" + String.valueOf(folder);
		fileHelper.createDirectory(thumbPath);

		String originPath = ORIGINAL_PATH + "/" + username + "/" + String.valueOf(folder);
		fileHelper.createDirectory(originPath);

		String tempPath = UPLOAD_PATH + "/" + username + "/" + String.valueOf(folder) + "/" + "temp";
		byte[] buffer = new byte[1024];
		for (String item : files) {
			String[] args = item.split("/");
			String fileName = args[0];
			String newName = args[1];
			File file = new File(tempPath + "/" + newName);
			if (file.getName().toLowerCase().endsWith(".zip")) {
				try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file), Charset.forName(charset));) {
					ZipEntry entry = zis.getNextEntry();
					while (entry != null) {
						if (!entry.isDirectory()
								&& entry.getName().toLowerCase().matches(".+(\\.jpg|\\.jpeg|\\.mp4)$")) {
							fileName = fileHelper.getName(entry.getName());
							String ext = fileHelper.getExtension(fileName);
							try {
								// unzip original image
								String originId = UUID.randomUUID().toString().replace("-", "") + ext;
								File original = new File(originPath + "/" + originId);
								FileOutputStream fos = new FileOutputStream(original);
								int len;
								while ((len = zis.read(buffer)) > 0) {
									fos.write(buffer, 0, len);
								}
								fos.close();

								// make thumbnail image
								String thumbId = UUID.randomUUID().toString().replace("-", "");

								// if mp4 file.
								if (".mp4".equals(ext.toLowerCase())) {
									if (price == 0) {
										File video = new File(thumbPath + "/" + "preview_" + thumbId + ".mp4");
										original.renameTo(video);
										BufferedImage stoped = AWTUtil
												.toBufferedImage(FrameGrab.getFrameFromFile(video, 60));

										File preview = new File(thumbPath + "/" + "preview_" + thumbId + ".jpg");
										File thumbnail = new File(thumbPath + "/" + "thumbnail_" + thumbId + ".jpg");
										BufferedImage marked = imageEditor.getPreview(stoped, 0);
										ImageIO.write(marked, "jpeg", preview);
										BufferedImage scaled = imageEditor.getThumbnail(marked);
										ImageIO.write(scaled, "jpeg", thumbnail);

										originId = thumbId + ".mp4";
										thumbId += ".jpg";
									} else {
										thumbId = null;
										original.delete();
									}
								} else {
									thumbId += ext;
									File preview = new File(thumbPath + "/" + "preview_" + thumbId);
									File thumbnail = new File(thumbPath + "/" + "thumbnail_" + thumbId);
									BufferedImage marked = imageEditor.getPreview(imageEditor.rotateImage(original),
											price);
									ImageIO.write(marked, "jpeg", preview);
									BufferedImage scaled = imageEditor.getThumbnail(marked);
									ImageIO.write(scaled, "jpeg", thumbnail);
								}

								if (thumbId != null) {
									// insert to db
									PhotoEntity photo = new PhotoEntity();
									photo.setUsername(username);
									photo.setFolder(folder);
									photo.setThumbnail(thumbId);
									photo.setOriginal(originId);
									photo.setFilename(fileName);
									photo.setPrice(price);
									photo.setCreatedt(DateTime.now());
									photoService.insertPhoto(photo);
								}
							} catch (Exception e) {
								logger.warn("Exception", e);
							}
						}
						entry = zis.getNextEntry();
					}
					zis.closeEntry();
				} catch (Exception e) {
					logger.warn("Exception", e);
				}
				file.delete();
			} else {
				String ext = fileHelper.getExtension(file.getName());
				try {
					// move original image
					String originId = UUID.randomUUID().toString().replace("-", "") + ext;
					File original = new File(originPath + "/" + originId);
					file.renameTo(original);

					// make thumbnail image
					String thumbId = UUID.randomUUID().toString().replace("-", "");

					// if mp4 file.
					if (".mp4".equals(ext.toLowerCase())) {
						if (price == 0) {
							File video = new File(thumbPath + "/" + "preview_" + thumbId + ".mp4");
							original.renameTo(video);
							BufferedImage stoped = AWTUtil.toBufferedImage(FrameGrab.getFrameFromFile(video, 60));

							File preview = new File(thumbPath + "/" + "preview_" + thumbId + ".jpg");
							File thumbnail = new File(thumbPath + "/" + "thumbnail_" + thumbId + ".jpg");
							BufferedImage marked = imageEditor.getPreview(stoped, 0);
							ImageIO.write(marked, "jpeg", preview);
							BufferedImage scaled = imageEditor.getThumbnail(marked);
							ImageIO.write(scaled, "jpeg", thumbnail);

							originId = thumbId + ".mp4";
							thumbId += ".jpg";
						} else {
							thumbId = null;
							original.delete();
						}
					} else {
						thumbId += ext;
						File preview = new File(thumbPath + "/" + "preview_" + thumbId);
						File thumbnail = new File(thumbPath + "/" + "thumbnail_" + thumbId);
						BufferedImage marked = imageEditor.getPreview(imageEditor.rotateImage(original), price);
						ImageIO.write(marked, "jpeg", preview);
						BufferedImage scaled = imageEditor.getThumbnail(marked);
						ImageIO.write(scaled, "jpeg", thumbnail);
					}

					if (thumbId != null) {
						// insert to db
						PhotoEntity photo = new PhotoEntity();
						photo.setUsername(username);
						photo.setFolder(folder);
						photo.setThumbnail(thumbId);
						photo.setOriginal(originId);
						photo.setFilename(fileName);
						photo.setPrice(price);
						photo.setCreatedt(DateTime.now());
						photoService.insertPhoto(photo);
					}
				} catch (Exception e) {
					logger.warn("Exception", e);
				}
			}
		}
	}
}
