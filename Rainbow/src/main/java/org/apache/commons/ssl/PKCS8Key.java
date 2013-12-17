/*
 * $HeadURL: http://juliusdavies.ca/svn/not-yet-commons-ssl/tags/commons-ssl-0.3.11/src/java/org/apache/commons/ssl/PKCS8Key.java $
 * $Revision: 153 $
 * $Date: 2009-09-15 22:40:53 -0700 (Tue, 15 Sep 2009) $
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.RC5ParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Utility for decrypting PKCS8 private keys.  Way easier to use than
 * javax.crypto.EncryptedPrivateKeyInfo since all you need is the byte[] array
 * and the password.  You don't need to know anything else about the PKCS8
 * key you pass in.
 * </p><p>
 * Can handle base64 PEM, or raw DER.
 * Can handle PKCS8 Version 1.5 and 2.0.
 * Can also handle OpenSSL encrypted or unencrypted private keys (DSA or RSA).
 * </p><p>
 * The PKCS12 key derivation (the "pkcs12()" method) comes from BouncyCastle.
 * </p>
 *
 * @author Credit Union Central of British Columbia
 * @author <a href="http://www.cucbc.com/">www.cucbc.com</a>
 * @author <a href="mailto:juliusdavies@cucbc.com">juliusdavies@cucbc.com</a>
 * @author <a href="bouncycastle.org">bouncycastle.org</a>
 * @since 7-Nov-2006
 */
public class PKCS8Key {
    public static Cipher generateCipher(String cipher, String mode,
                                        final DerivedKey dk,
                                        final boolean des2,
                                        final byte[] iv,
                                        final boolean decryptMode)
        throws NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeyException, InvalidAlgorithmParameterException {
        if (des2 && dk.key.length >= 24) {
            // copy first 8 bytes into last 8 bytes to create 2DES key.
            System.arraycopy(dk.key, 0, dk.key, 16, 8);
        }

        final int keySize = dk.key.length * 8;
        cipher = cipher.trim();
        String cipherUpper = cipher.toUpperCase();
        mode = mode.trim().toUpperCase();
        // Is the cipher even available?
        Cipher.getInstance(cipher);
        String padding = "PKCS5Padding";
        if (mode.startsWith("CFB") || mode.startsWith("OFB")) {
            padding = "NoPadding";
        }

        String transformation = cipher + "/" + mode + "/" + padding;
        if (cipherUpper.startsWith("RC4")) {
            // RC4 does not take mode or padding.
            transformation = cipher;
        }

        SecretKey secret = new SecretKeySpec(dk.key, cipher);
        IvParameterSpec ivParams;
        if (iv != null) {
            ivParams = new IvParameterSpec(iv);
        } else {
            ivParams = dk.iv != null ? new IvParameterSpec(dk.iv) : null;
        }

        Cipher c = Cipher.getInstance(transformation);
        int cipherMode = Cipher.ENCRYPT_MODE;
        if (decryptMode) {
            cipherMode = Cipher.DECRYPT_MODE;
        }

        // RC2 requires special params to inform engine of keysize.
        if (cipherUpper.startsWith("RC2")) {
            RC2ParameterSpec rcParams;
            if (mode.startsWith("ECB") || ivParams == null) {
                // ECB doesn't take an IV.
                rcParams = new RC2ParameterSpec(keySize);
            } else {
                rcParams = new RC2ParameterSpec(keySize, ivParams.getIV());
            }
            c.init(cipherMode, secret, rcParams);
        } else if (cipherUpper.startsWith("RC5")) {
            RC5ParameterSpec rcParams;
            if (mode.startsWith("ECB") || ivParams == null) {
                // ECB doesn't take an IV.
                rcParams = new RC5ParameterSpec(16, 12, 32);
            } else {
                rcParams = new RC5ParameterSpec(16, 12, 32, ivParams.getIV());
            }
            c.init(cipherMode, secret, rcParams);
        } else if (mode.startsWith("ECB") || cipherUpper.startsWith("RC4")) {
            // RC4 doesn't require any params.
            // Any cipher using ECB does not require an IV.
            c.init(cipherMode, secret);
        } else {
            // DES, DESede, AES, BlowFish require IVParams (when in CBC, CFB,
            // or OFB mode).  (In ECB mode they don't require IVParams).
            c.init(cipherMode, secret, ivParams);
        }
        return c;
    }
}
