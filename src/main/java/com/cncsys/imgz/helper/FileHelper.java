package com.cncsys.imgz.helper;

import java.io.File;

import org.springframework.stereotype.Component;

@Component
public class FileHelper {

	public String getExtension(String file) {
		int point = file.lastIndexOf(".");
		if (point != -1) {
			return file.substring(point);
		}
		return "";
	}

	public void createDirectory(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public void delete(File file) {
		if (!file.exists())
			return;

		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				this.delete(child);
			}
		}
		file.delete();
	}
}
