/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.rest;

/**
 * Holding accessKey and accessSecret for REST authentication.
 *
 * @author jejkal
 */
public final class SimpleRESTContext {

  /**
   * Key of the authentication.
   */
  private String accessKey = null;
  /**
   * Secret of the authentication.
   */
  private String accessSecret = null;

  /**
   * Constructor with key and secret.
   *
   * @param pKey key of the credentials.
   * @param pSecret secret of the credentials.
   */
  public SimpleRESTContext(String pKey, String pSecret) {
    setAccessKey(pKey);
    setAccessSecret(pSecret);
  }

  /**
   * Set key.
   *
   * @param accessKey new key
   */
  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  /**
   * Get key.
   *
   * @return key.
   */
  public String getAccessKey() {
    return accessKey;
  }

  /**
   * Set secret.
   *
   * @param accessSecret new secret
   */
  public void setAccessSecret(String accessSecret) {
    this.accessSecret = accessSecret;
  }

  /**
   * Get secret.
   *
   * @return secret.
   */
  public String getAccessSecret() {
    return accessSecret;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SimpleRESTContext)) {
      return false;
    }

    SimpleRESTContext theOther = (SimpleRESTContext) obj;

    //return TRUE only if accesskey and accesssecret are not null for both objects and if they are equal
    return getAccessKey() != null && theOther.getAccessKey() != null && getAccessKey().equals(theOther.getAccessKey())
            && getAccessSecret() != null && theOther.getAccessSecret() != null && getAccessSecret().equals(theOther.getAccessSecret());
  }

  @Override
  public int hashCode() {
    //This hash implementation should take care, that two objects only have the same hashcode if their fields are set and equal.
    //If a field is not set, a random value is added to the hash to reflect inequality.
    int hash = 5;
    hash = 97 * hash + (this.accessKey != null ? this.accessKey.hashCode() : (int) Math.rint(Math.random() * 1000));
    hash = 97 * hash + (this.accessSecret != null ? this.accessSecret.hashCode() : (int) Math.rint(Math.random() * 1000));
    return hash;
  }

  @Override
  public String toString() {
    return "[" + getAccessKey() + "/" + getAccessSecret() + "]";
  }

}
