/*
 * Copyright (C) 2013 Luca Casartelli luca@plasticpanda.com, Plastic Panda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plasticpanda.rainbow.utils;

import android.util.Base64;

import org.apache.commons.ssl.OpenSSL;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class SecurityUtils {

    private static final String AES_ALGORITHM = "aes256";
    private static final char[] SECRET_KEY = "prisco".toCharArray();

    /**
     * @param cipherText encrypted text
     * @return decrypted text
     */
    public static String decrypt(String cipherText) {
        String decryptedText;
        try {
            byte[] encryptedData = Base64.decode(cipherText, Base64.DEFAULT);
            byte[] _decryptedData = OpenSSL.decrypt(AES_ALGORITHM, SECRET_KEY, encryptedData);
            decryptedText = new String(_decryptedData);
        } catch (IOException e) {
            decryptedText = cipherText;
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            decryptedText = cipherText;
            e.printStackTrace();
        } catch (RuntimeException e) {
            decryptedText = cipherText;
            e.printStackTrace();
        }
        return decryptedText;
    }

    public static String encrypt(String text) {
        String encrypted;
        try {
            byte[] encryptedData = OpenSSL.encrypt(AES_ALGORITHM, SECRET_KEY, text.getBytes("UTF8"), false);
            encrypted = Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (IOException e) {
            encrypted = text;
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            encrypted = text;
            e.printStackTrace();
        } catch (RuntimeException e) {
            encrypted = text;
            e.printStackTrace();
        }
        return encrypted;
    }
}
