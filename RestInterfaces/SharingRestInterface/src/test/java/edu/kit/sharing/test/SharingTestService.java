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
package edu.kit.sharing.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.impl.GrantSet;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.sharing.services.interfaces.ISharingService;
import edu.kit.dama.rest.sharing.types.GrantSetWrapper;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import edu.kit.dama.rest.sharing.types.GroupIdWrapper;
import edu.kit.dama.rest.sharing.types.ReferenceIdWrapper;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.util.Constants;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author dapp
 */
@Path("/SharingTest")
public class SharingTestService implements ISharingService {

  static List<SecurableResourceId> resources = new LinkedList<>();
  static HashMap<SecurableResourceId, GrantSet> resourceGrants = new HashMap<>();
  static List<ReferenceId> references = new LinkedList<>();

  protected static void factoryGrant() {

    SecurableResource sr = new SecurableResource();
    sr.setId(100l);
    Grant g = new Grant();
    GrantSet gs = new GrantSet();
    g.setId(200l);
    g.setGrantedRole(Role.MEMBER);
    g.setGrantee(new User("testuser", Role.ADMINISTRATOR));
    gs.getGrants().add(g);
    resourceGrants.put(sr.getSecurableResourceId(), gs);
  }

  @Override
  public StreamingOutput createReference(String pDomain,
          String pDomainUniqueId, String pReferenceGroupId, String pRole, String pGroupId, HttpContext hc) {

    SecurableResourceId resId = new SecurableResourceId(pDomain, pDomainUniqueId);

    CollectionUtils.find(references, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return false;
      }
    });

    ReferenceId rid = new ReferenceId(resId, new GroupId(pReferenceGroupId));
    references.add(rid);

    return createObjectGraphStream(ReferenceIdWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new ReferenceIdWrapper(rid));
  }

  @Override
  public StreamingOutput getReferences(String pDomain, String pDomainUniqueId,
          String pGroupId, HttpContext hc) {

    final GroupId gid = new GroupId(pGroupId);

    Collection rs = CollectionUtils.select(references, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return ((ReferenceId) o).getGroupId().equals(gid);
      }
    });

    List<ReferenceId> rl = new LinkedList<>(rs);

    return createObjectGraphStream(ReferenceIdWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new ReferenceIdWrapper(rl));
  }

  @Override
  public StreamingOutput getReferencedGroups(String pDomain,
          String pDomainUniqueId, String pRole, String pGroupId, HttpContext hc) {

    final SecurableResourceId rid = new SecurableResourceId(pDomain,
            pDomainUniqueId);

    Collection gs = CollectionUtils.select(references, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return ((ReferenceId) o).getResourceId().equals(rid);
      }
    });

    List<GroupId> gl = new LinkedList<>(gs);

    return createObjectGraphStream(GroupIdWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new GroupIdWrapper(gl));
  }

  @Override
  public Response deleteReference(String pDomain, String pDomainUniqueId,
          String pGroupId, HttpContext hc) {

    final SecurableResourceId rid = new SecurableResourceId(pDomain,
            pDomainUniqueId);
    final GroupId gid = new GroupId(pGroupId);

    ReferenceId rr = (ReferenceId) CollectionUtils.find(references,
            new Predicate() {

              @Override
              public boolean evaluate(Object o) {
                ReferenceId r = (ReferenceId) o;
                return r.getSecurableResourceId() == rid
                && r.getGroupId() == gid;
              }
            });

    if (rr != null) {
      references.remove(rr);
    }

    return Response.ok().build();
  }

  @Override
  public StreamingOutput getGrantSetForResource(String pDomain,
          String pDomainUniqueId, String pGroupId, HttpContext hc) {

    SecurableResourceId rid = new SecurableResourceId(pDomain,
            pDomainUniqueId);

    GrantSet grs = resourceGrants.get(rid);

    return createObjectGraphStream(GrantSetWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new GrantSetWrapper(grs));
  }

  @Override
  public StreamingOutput getGrantById(final Long pId, String pGroupId,
          HttpContext hc) {

    for (GrantSet gg : resourceGrants.values()) {
      for (Grant g : gg.getGrants()) {
        if (g.getId() == pId) {
          return createObjectGraphStream(GrantWrapper.class,
                  Constants.REST_SIMPLE_OBJECT_GRAPH, new GrantWrapper(g));
        }
      }
    }

    return createObjectGraphStream(GrantWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new GrantWrapper(0));
  }

  @Override
  public StreamingOutput createGrant(String pDomain, String pDomainUniqueId,
          String pUserId, String pGroupId, String pRole, HttpContext hc) {

    SecurableResourceId rid = new SecurableResourceId(pDomain,
            pDomainUniqueId);

    GrantSet grs = new GrantSet(new SecurableResource(rid), Role.MEMBER);
    Grant gr = new Grant(new User(pUserId, Role.valueOf(pRole)), Role.
            valueOf(pRole), grs);
    gr.setId(300l);
    grs.getGrants().add(gr);
    resourceGrants.put(rid, grs);

    return createObjectGraphStream(GrantWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new GrantWrapper(gr));
  }

  @Override
  public Response revokeGrant(final Long pId, String pGroupId, HttpContext hc) {

    for (GrantSet gg : resourceGrants.values()) {
      for (Grant g : gg.getGrants()) {
        if (g.getId() == pId) {
          gg.getGrants().remove(g);
        }
      }
    }

    return Response.ok().build();
  }

  @Override
  public Response revokeAllGrants(String pDomain, String pDomainUniqueId,
          String pGroupId, HttpContext hc) {

    return Response.ok().build();
  }

  @Override
  public StreamingOutput updateGrant(Long pId, String pGroupId, String pRole,
          HttpContext hc) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public StreamingOutput getAuthorizedUsers(String pDomain,
          String pDomainUniqueId, String pRole, String pGroupId, HttpContext hc) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Response checkService() {
    return Response.status(200).entity(new CheckServiceResponse("SharingTest", ServiceStatus.OK)).build();
  }

}
