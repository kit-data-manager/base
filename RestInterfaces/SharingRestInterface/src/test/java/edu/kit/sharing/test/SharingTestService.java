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
import edu.kit.dama.authorization.entities.IDefaultGrant;
import edu.kit.dama.authorization.entities.IDefaultReferenceId;
import edu.kit.dama.authorization.entities.ISimpleGrantSet;
import edu.kit.dama.authorization.entities.ISimpleGroupId;
import edu.kit.dama.authorization.entities.ISimpleUserId;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.impl.GrantSet;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.sharing.services.interfaces.ISharingService;
import edu.kit.dama.rest.sharing.types.GrantSetWrapper;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import edu.kit.dama.rest.sharing.types.GroupIdWrapper;
import edu.kit.dama.rest.sharing.types.ReferenceIdWrapper;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
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
    public IEntityWrapper<? extends IDefaultReferenceId> createReference(String pDomain, String pDomainUniqueId, String pReferenceGroupId, String pRole, String pGroupId, HttpContext hc) {
        SecurableResourceId resId = new SecurableResourceId(pDomain, pDomainUniqueId);

        CollectionUtils.find(references, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return false;
            }
        });

        ReferenceId rid = new ReferenceId(resId, new GroupId(pReferenceGroupId));
        references.add(rid);

        return new ReferenceIdWrapper(rid);
    }

    @Override
    public IEntityWrapper<? extends IDefaultReferenceId> getReferences(String pDomain, String pDomainUniqueId, String pGroupId, HttpContext hc) {
        final GroupId gid = new GroupId(pGroupId);

        Collection rs = CollectionUtils.select(references, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((ReferenceId) o).getGroupId().equals(gid);
            }
        });
        
        List<ReferenceId> referenceIds = new LinkedList<>();
        for (Object r : rs) {
            referenceIds.add(((ReferenceId) r));
        }
        return new ReferenceIdWrapper(referenceIds);
    }

    @Override
    public IEntityWrapper<? extends ISimpleGroupId> getReferencedGroups(String pDomain, String pDomainUniqueId, String pRole, String pGroupId, HttpContext hc) {
        final SecurableResourceId rid = new SecurableResourceId(pDomain, pDomainUniqueId);

        Collection gs = CollectionUtils.select(references, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((ReferenceId) o).getResourceId().equals(rid);
            }
        });

        List<GroupId> groupIds = new LinkedList<>();
        for (Object r : gs) {
            groupIds.add(((ReferenceId) r).getGroupId());
        }

        return new GroupIdWrapper(groupIds);
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
        references.remove(rr);
        return Response.ok().build();
    }

    @Override
    public IEntityWrapper<? extends ISimpleGrantSet> getGrantSetForResource(String pDomain, String pDomainUniqueId, String pGroupId, HttpContext hc) {
        SecurableResourceId rid = new SecurableResourceId(pDomain,
                pDomainUniqueId);

        return new GrantSetWrapper(resourceGrants.get(rid));
    }

    @Override
    public IEntityWrapper<? extends IDefaultGrant> getGrantById(Long pId, String pGroupId, HttpContext hc) {

        for (GrantSet gg : resourceGrants.values()) {
            for (Grant g : gg.getGrants()) {
                if (Objects.equals(g.getId(), pId)) {
                    return new GrantWrapper(g);
                }
            }
        }

        return new GrantWrapper(0);
    }

    @Override
    public IEntityWrapper<? extends IDefaultGrant> createGrant(String pDomain, String pDomainUniqueId, String pUserId, String pGroupId, String pRole, HttpContext hc) {
        SecurableResourceId rid = new SecurableResourceId(pDomain,
                pDomainUniqueId);

        GrantSet grs = new GrantSet(new SecurableResource(rid), Role.MEMBER);
        Grant gr = new Grant(new User(pUserId, Role.valueOf(pRole)), Role.
                valueOf(pRole), grs);
        gr.setId(300l);
        grs.getGrants().add(gr);
        resourceGrants.put(rid, grs);

        return new GrantWrapper(gr);
    }

    @Override
    public Response revokeGrant(final Long pId, String pGroupId, HttpContext hc) {
        for (GrantSet gg : resourceGrants.values()) {
            for (Grant g : gg.getGrants()) {
                if (Objects.equals(g.getId(), pId)) {
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
    public IEntityWrapper<? extends IDefaultGrant> updateGrant(Long pId, String pGroupId, String pRole, HttpContext hc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IEntityWrapper<? extends ISimpleUserId> getAuthorizedUsers(String pDomain, String pDomainUniqueId, String pRole, String pGroupId, HttpContext hc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response checkService() {
        return Response.status(200).entity(new CheckServiceResponse("SharingTest", ServiceStatus.OK)).build();
    }

}
