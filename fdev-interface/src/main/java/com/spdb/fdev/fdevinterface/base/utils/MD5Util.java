package com.spdb.fdev.fdevinterface.base.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

public class MD5Util {

    private MD5Util() {
    }

    /**
     * 对字符串进行MD5
     *
     * @param salt
     * @param psd
     * @return
     */
    public static String encoder(String salt, String psd) {
        StringBuilder sb = new StringBuilder();
        try {
            //1.指定加密算法
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            //2.将需要加密的字符串转换为byte，然后哈西算法
            byte[] bs = digest.digest((salt + psd).getBytes());
            //3.遍历，让其生成32位字符串，固定写法
            for (byte b : bs) {
                int i = b & 0xff;
                String hexString = Integer.toHexString(i);
                if (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
        } catch (Exception e) {
            return sb.toString();
        }
        return sb.toString();
    }

    /**
     * 对文件进行MD5
     *
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static String encoderFile(String filePath) {
        StringBuilder md5 = new StringBuilder();
        try (InputStream in = new FileInputStream(filePath)) {
            byte[] buf = new byte[1024 * 1024];
            MessageDigest md5Digest = MessageDigest.getInstance("SHA-512");
            int num;
            do {
                num = in.read(buf);
                if (num > 0) {
                    md5Digest.update(buf, 0, num);
                }
            } while (num != -1);
            byte[] b = md5Digest.digest();
            for (int i = 0; i < b.length; i++) {
                // 每个字节转换成两位的十六进制数
                md5.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception e) {
            return md5.toString();
        }
        return md5.toString();
    }

    /**
     * 对文件夹进行MD5
     *
     * @param filePath 文件夹下所有文件路径
     * @return
     * @throws Exception
     */
    public static String encoderFiles(List<String> filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String path : filePath) {
            String jsonMD5 = MD5Util.encoderFile(path);
            stringBuilder.append(jsonMD5);
        }
        return MD5Util.encoder("", stringBuilder.toString());
    }

}
