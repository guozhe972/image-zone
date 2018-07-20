package com.cncsys.imgz.helper;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class CodeParser {

	@Value("${encrypt.password}")
	private String ENCRYPT_PASSWORD;

	@Value("${encrypt.salt}")
	private String ENCRYPT_SALT;

	private TextEncryptor txtEncryptor;

	@PostConstruct
	private void init() {
		txtEncryptor = Encryptors.delux(ENCRYPT_PASSWORD, ENCRYPT_SALT);
    }

	public String encrypt(String code) {
		return txtEncryptor.encrypt(code);
	}

	public String decrypt(String encode) {
		return txtEncryptor.decrypt(encode);
	}
}
