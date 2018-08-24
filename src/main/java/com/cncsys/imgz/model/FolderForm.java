package com.cncsys.imgz.model;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Data;

@Data
public class FolderForm {

	public static interface Upload {
	};

	public static interface Share {
	};

	private int seq;
	private String username;
	private String name;
	private boolean shared;
	private String guest;
	private String password;
	private List<MultipartFile> files;
	@JsonFormat(shape = Shape.STRING)
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate expiredt;
}
