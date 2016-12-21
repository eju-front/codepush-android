package com.eju.cy.codepush;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Md5 util
 *
 * @author SidneyXU
 */
/* package */ class Md5 {

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z'};

    private Md5() {
    }

    public static String encode(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return bytesToHexString(digest.digest(string.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeToBase64(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(string.getBytes("UTF-8"));
            byte[] data = digest.digest();
            return Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeFile(File file) {
        try {
            return encode(new FileInputStream(file));
        } catch (FileNotFoundException ignored) {
        }
        return null;
    }

    public static String encode(InputStream in) {
        DigestInputStream dis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(in, digest);
            byte[] buffer = new byte[1024];
            while ((dis.read(buffer)) > 0) {
            }
            byte[] result = digest.digest();
            return bytesToHexString(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        final char[] buf = new char[bytes.length * 2];

        byte b;
        int c = 0;
        for (byte aByte : bytes) {
            b = aByte;
            buf[c++] = DIGITS[(b >> 4) & 0xf];
            buf[c++] = DIGITS[b & 0xf];
        }

        return new String(buf);
    }
}
