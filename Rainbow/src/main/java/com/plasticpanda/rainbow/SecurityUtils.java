package com.plasticpanda.rainbow;

import android.util.Base64;

import org.apache.commons.ssl.OpenSSL;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;


public class SecurityUtils {

    private static final String AES_ALGORITHM = "aes256";
    private static final char[] SECRET_KEY = "prisco".toCharArray();

    /**
     * @param cipherText encrypted text
     * @return decrypted text
     */
    public static String decrypt(String cipherText) {
        byte[] encryptedData = Base64.decode(cipherText, Base64.DEFAULT);
        String decryptedText = "";
        try {
            byte[] _decryptedData = OpenSSL.decrypt(AES_ALGORITHM, SECRET_KEY, encryptedData);
            decryptedText = new String(_decryptedData);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return decryptedText;
    }

    public static String encrypt(String text) {
        String encrypted = null;
        try {
            byte[] encryptedData = OpenSSL.encrypt(AES_ALGORITHM, SECRET_KEY, text.getBytes("UTF8"), false);
            encrypted = Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return encrypted;
    }
}
