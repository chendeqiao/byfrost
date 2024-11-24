package com.intelligence.browser.markLock.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5Util {

    public static String getStringMD5(String plainText) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
        } catch (Exception e) {
            return null;
        }
        return encodeHex(md.digest());
    }

    public static String encodeHex(byte[] data) {
        if (data == null) {
            return null;
        }
        String HEXES = "0123456789abcdef";
        int len = data.length;
        StringBuilder hex = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            hex.append("0123456789abcdef".charAt((data[i] & 0xF0) >>> 4));
            hex.append("0123456789abcdef".charAt(data[i] & 0xF));
        }
        return hex.toString();
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }



}
