/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;

/**
 * Small tool to encrypt and decrypt strings using AES/ECB/PKCS7Padding. In KIT
 * Data Manager this is used to encrypt access keys and tokens before they are
 * stored in the database.
 *
 * @author jejkal
 */
public final class CryptUtil {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CryptUtil.class);

    //16 byte secret obtained from clear key (default: qr2I9Hyp0CBhUUXj)
    public enum CRYPT_TYPE {

        ENCRYPT,
        DECRYPT
    }

    private final Cipher deCipher;
    private final Cipher enCipher;
    private static CryptUtil singleton = null;

    /**
     * Hidden constuctor.
     *
     * @param pSecret The secret used for the SecretKeySpec. The secret must
     * have a length of 128, 192 or 256 bits.
     */
    private CryptUtil(byte[] pSecret) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(pSecret, "AES");
            deCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new BouncyCastleProvider());
            deCipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            enCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new BouncyCastleProvider());
            enCipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Failed to create cipher instances.", e);
        }
    }

    /**
     * Hidden constuctor.
     */
    private CryptUtil() {
        this(DataManagerSettings.getSingleton().getStringProperty(
                DataManagerSettings.GENERAL_GLOBAL_SECRET, new String(
                        new byte[]{(byte) 113, (byte) 114, (byte) 50, (byte) 73, (byte) 57, (byte) 72, (byte) 121, (byte) 112, (byte) 48, (byte) 67, (byte) 66, (byte) 104, (byte) 85, (byte) 85, (byte) 88, (byte) 106}
                )
        ).getBytes());
    }

    /**
     * Get the singleton instance.
     *
     * @return The singleton.
     */
    public static synchronized CryptUtil getSingleton() {
        return getSingleton(null);
    }

    /**
     * Get the singleton instance.
     *
     * @param pSecret The secret used for de-/encryption. The secret must have a
     * length of 128, 192 or 256 bits.
     *
     * @return The singleton.
     */
    public static synchronized CryptUtil getSingleton(byte[] pSecret) {
        if (singleton == null) {
            if (pSecret != null) {
                singleton = new CryptUtil(pSecret);
            } else {
                singleton = new CryptUtil();
            }
        }
        return singleton;
    }

    /**
     * Decrypt a string using DES and return the base64 encoded string of the
     * encrypted bytes.
     *
     * @param pPlainString The plain string to encrypt.
     *
     * @return The encrypted and base64 encoded string.
     *
     * @throws NoSuchAlgorithmException Internal exception which should not
     * raise
     * @throws NoSuchPaddingException Internal exception which should not raise
     * @throws InvalidKeyException Internal exception which should not raise
     * @throws ShortBufferException Internal exception which should not raise
     * @throws IllegalBlockSizeException Internal exception which should not
     * raise
     * @throws BadPaddingException Internal exception which should not raise
     */
    public String encrypt(String pPlainString) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException {
        return performCryptographicOperation(pPlainString, CRYPT_TYPE.ENCRYPT);
    }

    /**
     * Decrypt a string to its plain format. The argument is a base64 encoded
     * string returned by encrypt(pPlainString).
     *
     * @param pBase64String The base64 encoded, encrypted input string obtained
     * by encrypt(pPlainString).
     *
     * @return The decoded plain text string.
     *
     * @throws NoSuchAlgorithmException Internal exception which should not
     * raise
     * @throws NoSuchPaddingException Internal exception which should not raise
     * @throws InvalidKeyException Internal exception which should not raise
     * @throws ShortBufferException Internal exception which should not raise
     * @throws IllegalBlockSizeException Internal exception which should not
     * raise
     * @throws BadPaddingException Internal exception which should not raise
     */
    public String decrypt(String pBase64String) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException {
        return performCryptographicOperation(pBase64String, CRYPT_TYPE.DECRYPT);
    }

    /**
     * Perform the actual cryptographic operation, which are CRYPT_TYPE.ENCRYPT
     * or CRYPT_TYPE.DECRYPT. Using the central method is necessary as it seems
     * to be the only way to allow a multithreaded access to Cipher instances.
     * All cipher operations are located in a synchronized in this method. The
     * public methods {@link #encrypt(java.lang.String)
     * } and {@link #decrypt(java.lang.String) } are making use of this method.
     *
     * @param pInputData Either the plaintext string to encode or the Base64
     * encoded string to decode.
     * @param pCryptType CRYPT_TYPE.ENCRYPT or CRYPT_TYPE.DECRYPT
     *
     * @return The en-/decrypted string.
     *
     * @throws NoSuchAlgorithmException Internal exception which should not
     * raise
     * @throws NoSuchPaddingException Internal exception which should not raise
     * @throws InvalidKeyException Internal exception which should not raise
     * @throws ShortBufferException Internal exception which should not raise
     * @throws IllegalBlockSizeException Internal exception which should not
     * raise
     * @throws BadPaddingException Internal exception which should not raise
     */
    private synchronized String performCryptographicOperation(String pInputData, CRYPT_TYPE pCryptType) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException {
        String result = null;
        if (pInputData == null) {
            LOGGER.warn("Input data is null. Cryptographic operation skipped, returning null.");
            return null;
        }
        switch (pCryptType) {
            case ENCRYPT:
                result = Base64.encodeBase64String(enCipher.doFinal(pInputData.getBytes(Charset.forName("US-ASCII"))));
                break;
            case DECRYPT:
                result = new String(deCipher.doFinal(Base64.decodeBase64(pInputData)), Charset.forName("US-ASCII"));
                break;
        }

        return result;
    }

    /**
     * Convert the provided string to a SHA1 representation and return it as hex
     * string.
     *
     * @param pString The plain string.
     *
     * @return The target string as SHA1toHex(SHA1(pString)).
     */
    public static String stringToSHA1(String pString) {
        return DigestUtils.sha1Hex(DigestUtils.sha1(pString));
    }

    private static void printCLIHelp() {
        printCLIHelp(null);
    }

    private static void printCLIHelp(String error) {
        StringBuilder builder = new StringBuilder();
        if (error != null) {
            builder.append(error);
        }
        builder.append("Usage: <Executable> ENCRYPT|DECRYPT <secret> <string>\n");
        builder.append("Encrypt/Decrypt <string> using the secret <secret>.");
        builder.append("   <secret>\tThe secret used to de-/encrypt <string>. Supported are secrets with a length of 128, 192 or 256 bits.");
        builder.append("   <string>\tThe string to de-/encrypt using <secret>.");
        System.out.println(builder.toString());
    }
}
