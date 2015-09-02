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
package edu.kit.dama.mdm.admin;

import edu.kit.dama.mdm.admin.exception.SecretDecryptionException;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.util.CryptUtil;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author jejkal
 */
@Entity
public class ServiceAccessToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String userId;
  private String serviceId;
  private String tokenKey;
  private String tokenSecret;

  /**
   * Creates a new access token. The secret is selected as random alphanumeric
   * string with a length of 16 characters.
   *
   * @param pUserId The user id this token is associated with.
   * @param pServiceId The service id this token is associated with.
   * @param pTokenKey The token key.
   *
   * @return A new random service access token.
   *
   * @throws SecretEncryptionException If the encryption of the secret fails.
   */
  public static ServiceAccessToken factoryRandomToken(String pUserId, String pServiceId, String pTokenKey) throws SecretEncryptionException {
    ServiceAccessToken result = new ServiceAccessToken(pUserId);
    result.setServiceId(pServiceId);
    result.setTokenKey(pTokenKey);
    result.setSecret(RandomStringUtils.randomAlphanumeric(16));
    return result;
  }

  /**
   * Default constructor
   */
  public ServiceAccessToken() {
  }

  /**
   * Create a new token for the provided user id. Key/secret and service id must
   * be generated and set afterwards.
   *
   * @param pUserId The user id this token is associated with.
   */
  public ServiceAccessToken(String pUserId) {
    userId = pUserId;
  }

  /**
   * Create a new token for the provided user and service id. Key/secret must be
   * generated and set afterwards.
   *
   * @param pUserId The user id this token is associated with.
   * @param pServiceId The service id this token is associated with.
   */
  public ServiceAccessToken(String pUserId, String pServiceId) {
    userId = pUserId;
    serviceId = pServiceId;
  }

  /**
   * Regenerate secret and key.
   *
   * @throws SecretEncryptionException If the regeneration fails.
   */
  public void regenerate() throws SecretEncryptionException {
    setTokenKey(RandomStringUtils.randomAlphanumeric(16));
    setSecret(RandomStringUtils.randomAlphanumeric(16));
  }

  /**
   * Get the id.
   *
   * @return The id.
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the id.
   *
   * @param id The id.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the user id.
   *
   * @return The user id.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the user id.
   *
   * @param userId The user id.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the service id.
   *
   * @return The service id.
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Set the service id.
   *
   * @param serviceId The service id.
   */
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   * Set the token key.
   *
   * @param pTokenKey The key.
   */
  public void setTokenKey(String pTokenKey) {
    tokenKey = pTokenKey;
  }

  /**
   * Get the token key.
   *
   * @return The key.
   */
  public String getTokenKey() {
    return tokenKey;
  }

  /**
   * Set the token secret.
   *
   * @param pTokenSecret The plain text secret.
   */
  public void setTokenSecret(String pTokenSecret) {
    tokenSecret = pTokenSecret;
  }

  /**
   * Get the token secret.
   *
   * @return The secret.
   */
  public String getTokenSecret() {
    return tokenSecret;
  }

  /**
   * Get the decrypted secret.
   *
   * @return The secret.
   *
   * @throws SecretDecryptionException if the decryption fails.
   */
  public String getSecret() throws SecretDecryptionException {
    try {
      return CryptUtil.getSingleton().decrypt(getTokenSecret());
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException ex) {
      throw new SecretDecryptionException("Failed to decrypt secret", ex);
    }
  }

  /**
   * Set and encrypt the secret.
   *
   * @param pSecret The plain secret.
   *
   * @throws SecretEncryptionException if the encryption fails.
   */
  public void setSecret(String pSecret) throws SecretEncryptionException {
    try {
      setTokenSecret(CryptUtil.getSingleton().encrypt(pSecret));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException ex) {
      throw new SecretEncryptionException("Failed to set secret", ex);
    }
  }
}
