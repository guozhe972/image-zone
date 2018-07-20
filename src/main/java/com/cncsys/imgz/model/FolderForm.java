package com.cncsys.imgz.model;

import java.io.Serializable;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Data;

@Data
public class FolderForm implements Serializable {
	private static final long serialVersionUID = 1L;

	public static interface Upload {
	};

	private int seq;
	private String name;
	private String status;
	private String guest;
	private List<MultipartFile> files;
	@JsonFormat(shape = Shape.STRING)
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate expiredt;
}
