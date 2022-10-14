package com.spdb.fdev.fdevenvconfig.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/5/26 8:57
 */
@Component
@RefreshScope
public class DES3Util {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${fdev.encrypt.key}")
    private String secretKey = "zxcvbnmasdfghjklqwertyui";
    @Value("${fdev.encrypt.vec}")
    private String vec = "12345678";
    private static final String ENCODING = "UTF-8";
    private static final String ALGORITHM = "DESede/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "DESede";

    public DES3Util() {
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getBytes(ENCODING), KEY_ALGORITHM),
                    new IvParameterSpec(vec.getBytes(ENCODING)));
            byte[] encrptyData = cipher.doFinal(plainText.getBytes(ENCODING));
            return Base64.getEncoder().encodeToString(encrptyData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            logger.info("Encrypt Error:{}", e.getMessage());
            throw new FdevException(ErrorConstants.ENCODE_ERROR);
        }
    }

    public String decrypt(String encryptText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes(ENCODING), KEY_ALGORITHM),
                    new IvParameterSpec(vec.getBytes(ENCODING)));
            byte[] decryptData = cipher.doFinal(Base64.getDecoder().decode(encryptText));
            return new String(decryptData, ENCODING);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            logger.info("Decrypt Error:{}", e.getMessage());
            throw new FdevException(ErrorConstants.DECODE_ERROR);
        }
    }

    public static void main(String[] args) {
        String plainText = "1234";
        DES3Util des = new DES3Util();
        String encrypt = des.encrypt(plainText);
        System.out.println(encrypt);
        System.out.println(des.decrypt(encrypt));
        System.out.println(des.decrypt(encrypt).equals(plainText));
    }

}
