/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
   * Hidden constuctor.
   *
   * @param pSecret The secret used for the SecretKeySpec. The secret must have
   * a length of 128, 192 or 256 bits.
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
   * Decrypt a string using DES and return the base64 encoded string of the
   * encrypted bytes.
   *
   * @param pPlainString The plain string to encrypt.
   *
   * @return The encrypted and base64 encoded string.
   *
   * @throws NoSuchAlgorithmException Internal exception which should not raise
   * @throws NoSuchPaddingException Internal exception which should not raise
   * @throws InvalidKeyException Internal exception which should not raise
   * @throws ShortBufferException Internal exception which should not raise
   * @throws IllegalBlockSizeException Internal exception which should not raise
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
   * @param pBase64String The base64 encoded, encrypted input string obtained by
   * encrypt(pPlainString).
   *
   * @return The decoded plain text string.
   *
   * @throws NoSuchAlgorithmException Internal exception which should not raise
   * @throws NoSuchPaddingException Internal exception which should not raise
   * @throws InvalidKeyException Internal exception which should not raise
   * @throws ShortBufferException Internal exception which should not raise
   * @throws IllegalBlockSizeException Internal exception which should not raise
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
   * Perform the actual cryptographic operation, which are CRYPT_TYPE.ENCRYPT or
   * CRYPT_TYPE.DECRYPT. Using the central method is necessary as it seems to be
   * the only way to allow a multithreaded access to Cipher instances. All
   * cipher operations are located in a synchronized in this method. The public
   * methods {@link #encrypt(java.lang.String)
   * } and {@link #decrypt(java.lang.String) } are making use of this method.
   *
   * @param pInputData Either the plaintext string to encode or the Base64
   * encoded string to decode.
   * @param pCryptType CRYPT_TYPE.ENCRYPT or CRYPT_TYPE.DECRYPT
   *
   * @return The en-/decrypted string.
   *
   * @throws NoSuchAlgorithmException Internal exception which should not raise
   * @throws NoSuchPaddingException Internal exception which should not raise
   * @throws InvalidKeyException Internal exception which should not raise
   * @throws ShortBufferException Internal exception which should not raise
   * @throws IllegalBlockSizeException Internal exception which should not raise
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

  /**
   * The main method.
   *
   * @param args The argument array.
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    /* final DockerClient docker = new DefaultDockerClient.Builder()
     .uri(URI.create("http://192.168.59.100:2375"))
     .build();
    
     System.out.println(docker.info().containers());
    
     docker.close();*/
    JSch jsch = new JSch();
    //jsch.addIdentity("C:/Users/jejkal.FZKA/.ssh/id_rsa");
    String key = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEpAIBAAKCAQEAq/8wcvDejvPfkPqy/rHXRsknYgfDAu/lDrbcTSU0ZKN1v2hW\n"
            + "u3l781bANEZDMGkalA54YBXcxpG1k1jy4MSA+sAjeB1S4LXcmNEQy+ZbNZ1SoG0r\n"
            + "9ryaD33nFSQRO59TJiFuaZl/4N52f8g0Ub5MjNxt+naiSguGgJERwzvJAZUnmiBj\n"
            + "cXoei080CZdtqsT6W87sge1iT5lFvzcfUiK5rjBOwpZrVwxSfme0FG6Q9Ly9jcFM\n"
            + "GIQR2Y/CEufPafswsgYlsz2PUNBD2Rgfv3KA9z1j/uhsxqOSchRiLgkqZIeTWHKK\n"
            + "9WxSiLvv4fRJ0BNC0z/6uTR1YxV6XesofzmbRwIDAQABAoIBAQCIUgctYpmTBdlx\n"
            + "SDIsSfoNIYt0WCCJKGgM5IPxJQbEqQW/QkxT5LKIKH7IL9Q6/2LQbDOePFWykHQw\n"
            + "p/RAj56Gn1i9b8hrT9jaygEdCqPJ97owarbIpa0ZkDlh+ScrcVjuxNqFGeNP8CJN\n"
            + "nvlQvJphnqgQNozkUQQ2zEQe0Di9ZhSgMSFJYlSsmEUbUDL3BRxnUtz7iWStyXv9\n"
            + "DoeGzqUTLGALFuFetcnQjBYuFYxuukXQCNSRN0JromiSmjt5AxMf2RH+HMBRVMfE\n"
            + "6kcqdHVJNcjDByMFFBywcMa9nYe2WJVG+aFg9GLdrCWoAwnlDd5ykev2p6eRilbt\n"
            + "xxlTOFL5AoGBANj4izOrt8SRmBA8AtCucV+syAYdQHCI2/YgCtWFc+fv5jabuT3d\n"
            + "X+ZhBsQkDLKO/6iPAj3YcKXbfFYU5TkSmiFA33vkMSxD/ITMXsiAKUfuvat+uEru\n"
            + "UHRaeMkTPcd41i5XqjHxTYXfXOoJMYsJFz/d77VCcQKDDNE+pLosDWIVAoGBAMrv\n"
            + "mg77FRDbXbgQSTYHVN0kYK458ZykERxukhpU6kHhxHAWoADKcM9Nvghb2B19jfGA\n"
            + "7Irlb+MH00MKm7b5/Je0eRzVatCAWacPMqZmYCqHwwJmNyZur99nd3Jf7021iE2A\n"
            + "VkgABqGOjOK5wJJ9UI6hEvztu+Hl5O1FyT9AXMrrAoGAYHMUBjMmbMY/76+NnNB+\n"
            + "64X2IOmt9JiSFzYlOsepP4hgMRRGY17eO54UEOrpjhKNSZPQ7kchxEjuW6HMR/Oh\n"
            + "+nhJIFzPExthzHLBC6YVM7nILM0XBZAsyZxSJyhbhSmNJGqp6KuYx5METbEqieDP\n"
            + "qf9xiITxkalJ8FZFidD9XgUCgYEAmK+PzSeyoN079Uojm0gG3OAK8etc00tKKIc0\n"
            + "3CT5oBoar28GcbHfEMpgaW+Y+g9GZednkdWwyjNQC4gGwrPyDb5WxY/5buizC5PO\n"
            + "uw4Z2sYMvlhjtEHhh4gUgfSWW3RzGkSuJjwGwTU84TV7I3yvyLB+VdkU3JEZHkC8\n"
            + "ex+U8YMCgYBTgrBmoXqCR3rGdAF/NE7jU9cj9QVKaABuSitAA8JGhC6GJmR33ZVg\n"
            + "0O7OTjipBJGjfauuCm3swMlTzf1WCkLTBKYcA+kkvd/vjJu/9h75a07HorKX8k+D\n"
            + "ajorxbxn9U5BAubzbsFLWhQTRdHgd7Nm4AVMxpHmXF1TXNl6tK4kVg==\n"
            + "-----END RSA PRIVATE KEY-----";

    String pub = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCr/zBy8N6O89+Q+rL+sddGySdiB8MC7+UOttxNJTRko3W/aFa7eXvzVsA0RkMwaRqUDnhgFdzGkbWTWPLgxID6wCN4HVLgtdyY0RDL5ls1nVKgbSv2vJoPfecVJBE7n1MmIW5pmX/g3nZ/yDRRvkyM3G36dqJKC4aAkRHDO8kBlSeaIGNxeh6LTzQJl22qxPpbzuyB7WJPmUW/Nx9SIrmuME7ClmtXDFJ+Z7QUbpD0vL2NwUwYhBHZj8IS589p+zCyBiWzPY9Q0EPZGB+/coD3PWP+6GzGo5JyFGIuCSpkh5NYcor1bFKIu+/h9EnQE0LTP/q5NHVjFXpd6yh/OZtH jejkal@ipepc57";

    jsch.addIdentity("MyKey", key.getBytes(), pub.getBytes(), "test".getBytes());
    Session session = jsch.getSession("lsdf", "ipelsdf1.lsdf.kit.edu", 24);
    session.setConfig("StrictHostKeyChecking", "no");
    // session.setPassword("Gl0busTk");
    session.connect(2000);
    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand("echo Test\nexit\n");
    channel.setOutputStream(System.out);
    channel.setInputStream(null);
    ((ChannelExec) channel).setErrStream(System.err);
    InputStream in = channel.getInputStream();
    channel.connect();

    byte[] tmp = new byte[1024];
    while (true) {
      while (in.available() > 0) {
        int i = in.read(tmp, 0, 1024);
        if (i < 0) {
          break;
        }
        System.out.print(new String(tmp, 0, i));
      }

      if (channel.isClosed()) {
        if (in.available() > 0) {
          continue;
        }
        System.out.println("exit-status: " + channel.getExitStatus());
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }
    channel.disconnect();
    session.disconnect();
    if (true) {
      return;
    }

    int exitCode = 1;
    if (args.length < 3) {
      printCLIHelp();
    } else {
      CRYPT_TYPE type = CRYPT_TYPE.ENCRYPT;
      try {
        type = CRYPT_TYPE.valueOf(args[0]);
      } catch (IllegalArgumentException ex) {
        printCLIHelp("Invalid CRYPT_TYPE. Only " + CRYPT_TYPE.ENCRYPT + " and " + CRYPT_TYPE.DECRYPT + " are supported.");
        System.exit(1);
      }

      String secret = args[1];
      //only 128/192/256 bit secrets are supported
      if (secret.length() != 16 && secret.length() != 24 && secret.length() != 32) {
        printCLIHelp("Only 128, 192 or 256 bit secrets are supported.");
      } else {
        try {
          String string = args[2];
          CryptUtil theUtil = CryptUtil.getSingleton(secret.getBytes());
          switch (type) {
            case ENCRYPT:
              System.out.println(string + " -> " + theUtil.encrypt(string));
              exitCode = 0;
              break;
            default:
              System.out.println(string + " -> " + theUtil.decrypt(string));
              exitCode = 0;
              break;
          }
        } catch (Exception ex) {
          System.out.println("Failed to perform de-/encryption. Cause:");
          ex.printStackTrace();
        }
      }
    }
    System.exit(exitCode);
  }
}
