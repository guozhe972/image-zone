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

	public String getName(String file) {
		int point = file.lastIndexOf("/");
		if (point != -1) {
			return file.substring(point + 1);
		}
		return file;
	}

	public void deleteFile(String file) {
		File target = new File(file);
		if (target.exists())
			target.delete();
	}

	public void createDirectory(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public void deleteFolder(File path) {
		if (!path.exists())
			return;

		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				this.deleteFolder(file);
			}
		}
		path.delete();
	}

	public long getFolderSize(File path) {
		if (path.isFile())
			return path.length();

		long size = 0;
		for (File file : path.listFiles()) {
			if (path.isDirectory()) {
				size += this.getFolderSize(file);
			} else {
				size += file.length();
			}
		}
		return size;
	}
}
