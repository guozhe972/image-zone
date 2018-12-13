package com.cncsys.imgz.model;

import lombok.Data;

@Data
public class PhotoForm {

	public static interface Price {
	};

	private String username;
	private int folder;
	private String thumbnail;
	private String original;
	private int price;
	private boolean incart;

	private boolean video;

	public boolean isVideo() {
		this.video = false;
		if (this.original == null) {
			return this.video;
		}

		int point = this.original.lastIndexOf(".");
		if (point != -1) {
			if (".mp4".equals(this.original.substring(point).toLowerCase())) {
				this.video = true;
			}
		}
		return this.video;
	}
}
