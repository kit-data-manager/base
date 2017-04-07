/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.authorization.services.administration.impl;

import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.ResourceReference;
import edu.kit.dama.authorization.entities.impl.GrantSet;
import java.util.List;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.util.Constants;
import javax.persistence.*;

/**
 *
 * @author ochsenreither
 */
public class TestUtil {

  public static final IAuthorizationContext sysCtx = new AuthorizationContext(new UserId(Constants.SYSTEM_ADMIN), new GroupId(Constants.SYSTEM_GROUP), Role.ADMINISTRATOR);

  public static void clearDB() {
    PU.setPersistenceUnitName("AuthorizationPU");

    EntityManager em = PU.entityManager();

    em.getTransaction().begin();

    List<Membership> memberships = em.createQuery("SELECT m from Memberships m", Membership.class).getResultList();
    for (Membership membership : memberships) {
      em.remove(membership);
    }

    em.flush();

    List<ResourceReference> resourceReferences = em.createQuery("SELECT rr from ResourceReferences rr", ResourceReference.class).getResultList();
    for (ResourceReference resourceReference : resourceReferences) {
      em.remove(resourceReference);
    }

    em.flush();

    List<Grant> grants = em.createQuery("SELECT g from Grants g", Grant.class).getResultList();
    for (Grant grant : grants) {
      em.remove(grant);
    }

    em.flush();

    List<User> users = em.createQuery("SELECT u from Users u", User.class).getResultList();
    for (User user : users) {
      em.remove(user);
    }

    em.flush();

    List<Group> groups = em.createQuery("SELECT g from Groups g", Group.class).getResultList();
    for (Group group : groups) {
      em.remove(group);
    }

    em.flush();

//        List<GrantSet> grantSets = em.createQuery("SELECT gs from GrantSets gs", GrantSet.class).getResultList();
//        for (GrantSet grantSet : grantSets) {
//            grantSet.setResource(null);
//            em.merge(grantSet);
//            em.remove(grantSet);
//        }
//
//        em.flush();

    List<SecurableResource> resources = em.createQuery("SELECT r from Resources r", SecurableResource.class).getResultList();
    for (SecurableResource resource : resources) {
      GrantSet grantSet = resource.getGrantSet();
      resource.setGrantSet(null);
      //grantSet.setResource(null);
      em.remove(resource);
      if (null != grantSet) {
        em.remove(grantSet);
      }
    }

    Group group = new Group(Constants.SYSTEM_GROUP);
    User user = new User(Constants.SYSTEM_ADMIN, Role.ADMINISTRATOR);
    em.persist(group);
    em.persist(user);
    Membership membership = new Membership(user, Role.ADMINISTRATOR, group);
    em.persist(membership);
    group.getMemberships().add(membership);
    user.getMemberships().add(membership);

    em.getTransaction().commit();

    em.close();
  }
//
//    //// BEGIN self-tests ////
//    @Test
//    public void checkAvailableUsers() {
//        System.out.println("checkAvailableUsers");
//
//        // Now lets check the database and see if the created entries are there
//        // Create a fresh, new EntityManager
//        EntityManager em = PU.entityManager();
//
//        // Perform a simple query for all the Message entities
//        Query q = em.createQuery("SELECT u from Users u");
//
//        // We should have 40 Persons in the database
//        assertEquals(40, q.getResultList().size());
//
//        em.close();
//    }
//
//    @Test
//    public void checkGroup() {
//        System.out.println("checkGroup");
//
//        EntityManager em = PU.entityManager();
//        // Go through each of the entities and print out each of their
//        // messages, as well as the date on which it was created
//        Query q = em.createQuery("SELECT g from Groups g");
//
//        // We should have one family with 40 persons
//        assertEquals(2, q.getResultList().size());
//
////        int groupSize = ((Group) q.getSingleResult()).getMemberships().size();
////        System.out.println("There should be 40 entries in this group. Actual: " + groupSize);
////        assertTrue(groupSize == 40);
//        em.close();
//    }
//
//    @Test
//    public void checkMembership() {
//        System.out.println("checkMembership");
//
//        EntityManager em = PU.entityManager();
//
//        Query q1 = em.createQuery("SELECT m.user FROM Memberships m WHERE m.group.groupId = :groupId").setParameter("groupId", "1");
//        Query q2 = em.createQuery("SELECT m.user FROM Memberships m WHERE m.group.groupId = :groupId").setParameter("groupId", "2");
//
//        assertEquals(20, q1.getResultList().size());
//        assertEquals(20, q2.getResultList().size());
//
//        em.close();
//    }
//
//    @Test(expected = javax.persistence.NoResultException.class)
//    public void deleteUser() {
//        System.out.println("deleteUser");
//
//        EntityManager em = PU.entityManager();
//        em.getTransaction().begin();
//        TypedQuery<Membership> qm = em.createQuery("SELECT m FROM Memberships m WHERE m.user.userId = :userId", Membership.class);
//        qm.setParameter("userId", "1");
//        List<Membership> memberships = qm.getResultList();
//        for (Membership membership : memberships) {
//            em.remove(membership);
//        }
//
//        TypedQuery<User> qu = em.createQuery("SELECT u FROM Users u WHERE u.userId = :userId", User.class);
//        qu.setParameter("userId", "1");
//        User user = qu.getSingleResult();
//        em.remove(user);
//        em.getTransaction().commit();
//        User person = qu.getSingleResult();
//        em.close();
//    }
//
//    @Test
//    public void create() {
//        System.out.println("create");
//
//        EntityManager em = PU.entityManager();
//
//        Group group = new Group("42");
//        User groupManager = new User("4242", Role.MANAGER);
//
//        em.getTransaction().begin();
//
//        //Will this fail with EntityExistsException if the group already exists? => Test!
//        em.persist(groupManager);
//        em.persist(group);
//
//        Membership membership = new Membership(groupManager, Role.MANAGER, group);
//
//        em.getTransaction().commit();
//
//        int amountOfUsers = em.createQuery("SELECT u from Users u", User.class).getResultList().size();
//        assertTrue(amountOfUsers == 41);
//        User user = em.createQuery("SELECT u from Users u WHERE u.userId = :userId", User.class).setParameter("userId", groupManager.getUserId()).getSingleResult();
//
//        em.close();
//    }
  //// END self-tests ////
}
