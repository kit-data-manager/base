/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.authorization.util;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class LDAPHelper {

  private final static Logger LOGGER = LoggerFactory.getLogger(LDAPHelper.class);

  private String ldapServer = null;
  private String ldapSearchBase = null;
  private String ldapUser = null;
  private String ldapPassword = null;
  private boolean debug = false;

  /**
   * Default constructor allowing to provide LDAP server and search base. The
   * query credentials must be set separately via {@link #setQueryCredentials(java.lang.String, java.lang.String)
   * }.
   *
   * @param pServer The LDAP server.
   * @param pSearchBase The search base used to localize the search.
   */
  public LDAPHelper(String pServer, String pSearchBase) {
    this(pServer, pSearchBase, null, null);
  }

  /**
   * Default constructor allowing to provide LDAP server, search base and query
   * credentials.
   *
   * @param pServer The LDAP server.
   * @param pSearchBase The search base used to localize the search.
   * @param pUser The LDAP user.
   * @param pPassword The LDAP password for pUser.
   */
  public LDAPHelper(String pServer, String pSearchBase, String pUser, String pPassword) {
    if (pServer == null) {
      throw new IllegalArgumentException("Argument pServer must not be null");
    }
    if (pSearchBase == null) {
      throw new IllegalArgumentException("Argument pSearchBase must not be null");
    }
    ldapServer = pServer;
    ldapSearchBase = pSearchBase;
    setQueryCredentials(pUser, pPassword);
  }

  /**
   * Set the credentials used for queries. By default, the credentials may be
   * set in the constructor, but can be changed during runtime without creating
   * a new instance of LDAPHelper.
   *
   * @param pUser The user DN, e.g.
   * uid=user1234,ou=YourOU,ou=SubOU,dc=YourCompany,dc=edu
   * @param pPassword The LDAP password.
   */
  public final void setQueryCredentials(String pUser, String pPassword) {
    ldapUser = pUser;
    ldapPassword = pPassword;
  }

  /**
   * Enabled/disables the LDAP debugging. If pValue is true, a lot of
   * LDAP-related debugging output is written to StdOut. By default, debugging
   * is disabled.
   *
   * @param pValue TRUE = debugging mode is enabled.
   */
  public void setDebugMode(boolean pValue) {
    debug = pValue;
  }

  /**
   * Internal helper method to build the LDAP context.
   *
   * @return The LdapContext.
   *
   * @throws NamingException If something goes wrong.
   */
  public LdapContext getContext() throws NamingException {
    Hashtable<String, Object> env = new Hashtable<>();
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    if (ldapUser != null) {
      env.put(Context.SECURITY_PRINCIPAL, ldapUser);
    }
    if (ldapPassword != null) {
      env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
    }
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapServer);
    env.put("java.naming.ldap.attributes.binary", "objectSID");
    // the following is helpful in debugging errors
    if (debug) {
      env.put("com.sun.jndi.ldap.trace.ber", System.err);
    }
    return new InitialLdapContext(env, null);
  }

  /**
   * Validate a user by its mail and password. This method encapsulates a
   * two-step process:
   * <ul>
   * <li>Query LDAP server for user with mail 'pEmail'. This must be done using
   * privileged credentials set before, e.g. via the constructor.</li>
   * <li>Obtain the username from the search result and do another query using
   * the obtained username and the provided password.</li>
   * </ul>
   *
   * If both steps succeed, TRUE is returned. The steps may fail if the first
   * query fails due to wrong credentials or missing privileged, if no user was
   * found for the provided email or if the second step fails due to a wrong
   * password.
   *
   * @param pEmail The user email.
   * @param pUserPassword The user password used for the second query.
   *
   * @return TRUE if there is a user with the provided email and password.
   */
  public boolean validateUserByEmail(String pEmail, String pUserPassword) {
    SearchResult result1 = findUserByEmail(pEmail);
    if (result1 == null) {
      //no user found for email
      return false;
    } else {
      setQueryCredentials(result1.getNameInNamespace(), pUserPassword);
      SearchResult result2 = findUserByEmail(pEmail);
      return (result2 != null);
    }
  }

  /**
   * Find a user by email. For this purpose the search filter
   * (&amp;(objectClass=person)(mail=" + pMail + ")) is applied and the search
   * result is returned. SearchResult will be 'null' if creating the LDAPContext
   * fails, if nothing was found or if more than one result was found.
   *
   * @param pMail The email to search for.
   *
   * @return The SearchResult.
   */
  public SearchResult findUserByEmail(String pMail) {
    String searchFilter = "(&(objectClass=person)(mail=" + pMail + "))";

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration<SearchResult> results;
    try {
      results = getContext().search(ldapSearchBase, searchFilter, searchControls);
    } catch (NamingException ex) {
      //
      return null;
    }
    SearchResult searchResult = null;
    if (results.hasMoreElements()) {
      searchResult = (SearchResult) results.nextElement();

      //make sure there is not another item available, there should be only 1 match
      if (results.hasMoreElements()) {
        System.err.println("Matched multiple users for the mail: " + pMail);
        return null;
      }
    }

    return searchResult;
  }

  /**
   * Find a user by uid. For this purpose the search filter
   * (&amp;(objectClass=person)(uid=" + pUid + ")) is applied and the search
   * result is returned. SearchResult will be 'null' if creating the LDAPContext
   * fails, if nothing was found or if more than one result was found.
   *
   * @param pUid The email to search for.
   *
   * @return The SearchResult.
   */
  public SearchResult findUserByUid(String pUid) {
    String searchFilter = "(&(objectClass=person)(uid=" + pUid + "))";

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration<SearchResult> results;
    try {
      results = getContext().search(ldapSearchBase, searchFilter, searchControls);
    } catch (NamingException ex) {
      LOGGER.error("Failed to find user by id " + pUid, ex);
      return null;
    }
    SearchResult searchResult = null;
    if (results.hasMoreElements()) {
      searchResult = (SearchResult) results.nextElement();

      //make sure there is not another item available, there should be only 1 match
      if (results.hasMoreElements()) {
        System.err.println("Matched multiple users for the uid: " + pUid);
        return null;
      }
    }

    return searchResult;
  }

  /**
   * Find all entries of class posixGroup.
   *
   * @return The enumeration to walk through all search results.
   *
   * @throws NamingException If the query could not be performed.
   */
  public NamingEnumeration<SearchResult> findPosixGroups() throws NamingException {
    return findPosixGroups(null);
  }

  /**
   * Find all entries of class posixGroup fulfilling the provided filter, e.g.
   * <i>(cn=myGroup*)</i>, to access all posixGroups with a CN beginning with
   * <i>myGroup</i>.
   * If pGroupCnFilter is null, no filter will be used.
   *
   * @param pGroupCnFilter The CN filter term or null if no filter should be
   * used.
   *
   * @return The enumeration to walk through all search results.
   *
   * @throws NamingException If the query could not be performed.
   */
  public NamingEnumeration<SearchResult> findPosixGroups(String pGroupCnFilter) throws NamingException {
    String searchFilter;

    if (pGroupCnFilter != null) {
      searchFilter = "(&(objectClass=posixGroup)" + pGroupCnFilter + ")";
    } else {
      searchFilter = "(&(objectClass=posixGroup))";
    }

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    return getContext().search(ldapSearchBase, searchFilter, searchControls);
  }

  /**
   * Find all entries of class posixAccount.
   *
   * @return The enumeration to walk through all search results.
   *
   * @throws NamingException If the query could not be performed.
   */
  public NamingEnumeration<SearchResult> findPosixAccounts() throws NamingException {
    String searchFilter = "(&(objectClass=posixAccount))";
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    return getContext().search(ldapSearchBase, searchFilter, searchControls);
  }

  public static void main(String[] args) throws Exception {
    LDAPHelper helper = new LDAPHelper("ldaps://bwidm.scc.kit.edu", "ou=syncro,dc=anka,dc=de", "uid=fileservice-read,ou=admin,ou=syncro,dc=anka,dc=de", "bldjj409wdmjsd2zo30sfn");
//
//    Map<String, PosixGroup> groups = helper.getAllGroupsWithMembers();
//
//    Set<Entry<String, PosixGroup>> entries = groups.entrySet();
//    for (Entry<String, PosixGroup> entry : entries) {
//      System.out.println("Group: " + entry.getValue().getCn());
//      for (PosixAccount member : entry.getValue().getMembers()) {
//        System.out.println(" - " + member.getSn() + ", " + member.getGivenName());
//      }
//    }

    /* NamingEnumeration<SearchResult> result = helper.findPosixGroups();
     while (result.hasMoreElements()) {
     SearchResult res = result.next();
     Attributes atts = res.getAttributes();
     String cn = (String) atts.get("cn").get();
     System.out.println("Group: " + cn);
     for (String id : helper.getMemberUids(cn)) {
     System.out.println("- " + id);
     }

     }*/
//    while (result.hasMoreElements()) {
//      try {
        /*NamingEnumeration<? extends Attribute> attribs = result.next();//.getAttributes().getAll();
     while (attribs.hasMore()) {
     System.out.println(attribs.next());
     }*/
//        PosixGroup.fromSearchResult(result.next());
//
//      } catch (NamingException ex) {
//        ex.printStackTrace();
//      }
//    }
  }

  //
//  public String findGroupBySID(DirContext ctx, String ldapSearchBase, String sid) throws NamingException {
//    String searchFilter = "(&(objectClass=group)(objectSid=" + sid + "))";
//
//    SearchControls searchControls = new SearchControls();
//    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//
//    NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
//
//    if (results.hasMoreElements()) {
//      SearchResult searchResult = (SearchResult) results.nextElement();
//
//      //make sure there is not another item available, there should be only 1 match
//      if (results.hasMoreElements()) {
//        System.err.println("Matched multiple groups for the group with SID: " + sid);
//        return null;
//      } else {
//        return (String) searchResult.getAttributes().get("sAMAccountName").get();
//      }
//    }
//    return null;
//  }
//
//  public String getPrimaryGroupSID(SearchResult srLdapUser) throws NamingException {
//    byte[] objectSID = (byte[]) srLdapUser.getAttributes().get("objectSid").get();
//    String strPrimaryGroupID = (String) srLdapUser.getAttributes().get("primaryGroupID").get();
//
//    String strObjectSid = decodeSID(objectSID);
//
//    return strObjectSid.substring(0, strObjectSid.lastIndexOf('-') + 1) + strPrimaryGroupID;
//  }
//
//  /**
//   * The binary data is in the form: byte[0] - revision level byte[1] - count of
//   * sub-authorities byte[2-7] - 48 bit authority (big-endian) and then count x
//   * 32 bit sub authorities (little-endian)
//   *
//   * The String value is: S-Revision-Authority-SubAuthority[n]...
//   *
//   * Based on code from here -
//   * http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
//   */
//  public static String decodeSID(byte[] sid) {
//
//    final StringBuilder strSid = new StringBuilder("S-");
//
//    // get version
//    final int revision = sid[0];
//    strSid.append(Integer.toString(revision));
//
//    //next byte is the count of sub-authorities
//    final int countSubAuths = sid[1] & 0xFF;
//
//    //get the authority
//    long authority = 0;
//    //String rid = "";
//    for (int i = 2; i <= 7; i++) {
//      authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
//    }
//    strSid.append("-");
//    strSid.append(Long.toHexString(authority));
//
//    //iterate all the sub-auths
//    int offset = 8;
//    int size = 4; //4 bytes for each sub auth
//    for (int j = 0; j < countSubAuths; j++) {
//      long subAuthority = 0;
//      for (int k = 0; k < size; k++) {
//        subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
//      }
//
//      strSid.append("-");
//      strSid.append(subAuthority);
//
//      offset += size;
//    }
//
//    return strSid.toString();
//  }
}
