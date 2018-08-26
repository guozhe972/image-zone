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

	private TextEncryptor deluxEncryptor;
	private TextEncryptor queryEncryptor;

	@PostConstruct
	private void init() {
		deluxEncryptor = Encryptors.delux(ENCRYPT_PASSWORD, ENCRYPT_SALT);
		queryEncryptor = Encryptors.queryableText(ENCRYPT_PASSWORD, ENCRYPT_SALT);
	}

	public String encrypt(String plainText) {
		return deluxEncryptor.encrypt(plainText);
	}

	public String decrypt(String cipherText) {
		String rtn = null;
		try {
			rtn = deluxEncryptor.decrypt(cipherText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtn;
	}

	public String queryEncrypt(String plainText) {
		return queryEncryptor.encrypt(plainText);
	}

	public String queryDecrypt(String cipherText) {
		String rtn = null;
		try {
			rtn = queryEncryptor.decrypt(cipherText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtn;
	}
}
