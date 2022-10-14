package com.fdev.docmanage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EncryptionTool {
	
	public static Logger logger = LoggerFactory.getLogger(EncryptionTool.class);
	
    public static String getFileSha1(File file) {
        FileInputStream in = null;
        try{
            in = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1024 * 1024 * 10];
            int len = 0;
            while ((len = in.read(buffer)) > 0){
                digest.update(buffer, 0, len);
            }
            String sha1 = new BigInteger(1, digest.digest()).toString(16);
            int length = 40 - sha1.length();
            if (length > 0){
                for (int i = 0; i < length; i++){
                    sha1 = "0" + sha1;
                }
            }
            return sha1;
        }
        catch (IOException e){
        	logger.error(e.getMessage());
        }
        catch (NoSuchAlgorithmException e){
        	logger.error(e.getMessage());
        }
        finally{
            try{
                if (in != null){
                    in.close();
                }
            }catch (IOException e){
            	logger.error(e.getMessage());
            }
        }
        return "";
    }

}
