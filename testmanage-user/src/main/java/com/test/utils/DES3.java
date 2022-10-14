
package com.test.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@RefreshScope
public class DES3 {
	@Value("${fdev.encrypt.key}")
	private String secretKey;
	@Value("${fdev.encrypt.vec}")
	private String vec;
	private static final String ENCODING = "UTF-8";
	private static final String ALGORITHM = "DESede/CBC/PKCS5Padding";
	private static final String KEY_ALGORITHM = "DESede";

	public DES3() {
	}

	public DES3(String secretKey) {
		this.secretKey = secretKey;
	}

	public String encrypt(String plainText) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getBytes(ENCODING), KEY_ALGORITHM),
					new IvParameterSpec(vec.getBytes(ENCODING)));
			byte encrptyData[] = cipher.doFinal(plainText.getBytes(ENCODING));
			return Base64.getEncoder().encodeToString(encrptyData);
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("DES encrypt error!", e);
		}
	}

	public String decrypt(String encryptText) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes(ENCODING), KEY_ALGORITHM),
					new IvParameterSpec(vec.getBytes(ENCODING)));
			byte decryptData[] = cipher.doFinal(Base64.getDecoder().decode(encryptText));
			return new String(decryptData, ENCODING);
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("DES decrypt error!", e);
		}
	}

	public static void main(String[] args) {
		String plainText = "1234 ";
		DES3 des = new DES3();
		String encrypt = des.encrypt(plainText);
		System.out.println(encrypt);
		System.out.println(des.decrypt(encrypt));
		System.out.println(des.decrypt(encrypt).equals(plainText));
	}
}
