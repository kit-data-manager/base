/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.authorization.ldap.entities;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class PosixAccount {

  private final static Logger LOGGER = LoggerFactory.getLogger(PosixAccount.class);

  private String uid;
  private String cn;
  private String givenName;
  private String sn;
  private String homeDir;
  private String mail;
  private int uidNumber;
  private int gidNumber;
  private List<PosixGroup> memberships = new ArrayList<>();

  public PosixAccount(String uid, String cn, String givenName, String sn, String homeDir, String mail, int uidNumber, int gidNumber) {
    this.uid = uid;
    this.cn = cn;
    this.givenName = givenName;
    this.sn = sn;
    this.homeDir = homeDir;
    this.mail = mail;
    this.uidNumber = uidNumber;
    this.gidNumber = gidNumber;
  }

  public static PosixAccount fromSearchResult(SearchResult pResult) throws NamingException {
    Attributes attribs = pResult.getAttributes();
    Attribute uidAttrib = attribs.get("uid");
    String uid = (String) uidAttrib.get();
    Attribute cnAttrib = attribs.get("cn");
    String cn = (String) cnAttrib.get();
    Attribute givenNameAttrib = attribs.get("givenName");
    String givenName = (String) givenNameAttrib.get();
    Attribute snAttrib = attribs.get("sn");
    String sn = (String) snAttrib.get();
    Attribute homeDirAttrib = attribs.get("homeDirectory");
    String homeDir = (String) homeDirAttrib.get();
    Attribute mailAttrib = attribs.get("mail");
    String mail = (String) mailAttrib.get();
    Attribute uidNumberAttrib = attribs.get("uidNumber");
    String uidNumber = (String) uidNumberAttrib.get();
    Attribute gidNumberAttrib = attribs.get("gidNumber");
    String gidNumber = (String) gidNumberAttrib.get();
    LOGGER.debug("Creating posixAccount via new PosixAccount({}, {}, {}, {}, {}, {}, {}, {})", uid, cn, givenName, sn, homeDir, mail, uidNumber, gidNumber);
    return new PosixAccount(uid, cn, givenName, sn, homeDir, mail, Integer.parseInt(uidNumber), Integer.parseInt(gidNumber));
  }

  /**
   * @return the uid
   */
  public String getUid() {
    return uid;
  }

  /**
   * @param uid the uid to set
   */
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * @return the cn
   */
  public String getCn() {
    return cn;
  }

  /**
   * @param cn the cn to set
   */
  public void setCn(String cn) {
    this.cn = cn;
  }

  /**
   * @return the givenName
   */
  public String getGivenName() {
    return givenName;
  }

  /**
   * @param givenName the givenName to set
   */
  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  /**
   * @return the sn
   */
  public String getSn() {
    return sn;
  }

  /**
   * @param sn the sn to set
   */
  public void setSn(String sn) {
    this.sn = sn;
  }

  /**
   * @return the homeDir
   */
  public String getHomeDir() {
    return homeDir;
  }

  /**
   * @param homeDir the homeDir to set
   */
  public void setHomeDir(String homeDir) {
    this.homeDir = homeDir;
  }

  /**
   * @return the mail
   */
  public String getMail() {
    return mail;
  }

  /**
   * @param mail the mail to set
   */
  public void setMail(String mail) {
    this.mail = mail;
  }

  /**
   * @return the uidNumber
   */
  public int getUidNumber() {
    return uidNumber;
  }

  /**
   * @param uidNumber the uidNumber to set
   */
  public void setUidNumber(int uidNumber) {
    this.uidNumber = uidNumber;
  }

  /**
   * @return the gidNumber
   */
  public int getGidNumber() {
    return gidNumber;
  }

  /**
   * @param gidNumber the gidNumber to set
   */
  public void setGidNumber(int gidNumber) {
    this.gidNumber = gidNumber;
  }

  public void setMemberships(List<PosixGroup> memberships) {
    this.memberships = memberships;
  }

  public List<PosixGroup> getMemberships() {
    return memberships;
  }

}
