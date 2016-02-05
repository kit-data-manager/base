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
public class PosixGroup {

  private final static Logger LOGGER = LoggerFactory.getLogger(PosixGroup.class);

  private String uid;
  private String cn;
  private int gidNumber;
  private List<PosixAccount> members = new ArrayList<>();

  public PosixGroup(String uid, String cn, int gidNumber) {
    this.uid = uid;
    this.cn = cn;
    this.gidNumber = gidNumber;
  }

  public static PosixGroup fromSearchResult(SearchResult pResult) throws NamingException {
    Attributes attribs = pResult.getAttributes();

    Attribute uidAttrib = attribs.get("uid");
    String uid = (String) uidAttrib.get();
    Attribute cnAttrib = attribs.get("cn");
    String cn = (String) cnAttrib.get();
    Attribute gidNumberAttrib = attribs.get("gidNumber");
    String gidNumber = (String) gidNumberAttrib.get();
    LOGGER.debug("Creating PosixGroup via new PosixGroup({}, {}, {})", uid, cn, gidNumber);
    return new PosixGroup(uid, cn, Integer.parseInt(gidNumber));
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

  /**
   * @return the members
   */
  public List<PosixAccount> getMembers() {
    return members;
  }

  /**
   * @param members the members to set
   */
  public void setMembers(List<PosixAccount> members) {
    this.members = members;
  }

  public void addMember(PosixAccount pMember) {
    members.add(pMember);
  }

}
