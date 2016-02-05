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
package edu.kit.dama.authorization.annotations;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.authorization.services.administration.impl.TestUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author pasic
 */
public class FilterOutputTest {

  UserId userId = new UserId("UserA");
  GroupId groupId = new GroupId("GroupA");
  Role roleRestriction = Role.MANAGER;
  IAuthorizationContext context = new AuthorizationContext(userId, groupId, roleRestriction);
  IAuthorizationContext sysCtx = TestUtil.sysCtx;
  Role roleRequired = Role.MEMBER;
  List<ISecurableResource> securableResourceList = new ArrayList<ISecurableResource>();
  int resCount = 0;

    /**
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    public FilterOutputTest() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
    TestUtil.clearDB();
    UserServiceLocal.getSingleton().register(userId, roleRequired, sysCtx);
    GroupServiceLocal.getSingleton().create(groupId, userId, sysCtx);

  }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
  public static void setUpClass() throws Exception {
  }

    /**
     *
     * @throws Exception
     */
    @AfterClass
  public static void tearDownClass() throws Exception {
  }

    /**
     *
     */
    @Before
  public void setUp() {
  }

    /**
     *
     */
    @After
  public void tearDown() {
  }

  @FilterOutput(roleRequired = Role.ADMINISTRATOR)
  Collection<ISecurableResource> testNoResPasses(@Context IAuthorizationContext ctx) throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    List<ISecurableResource> output = new ArrayList<ISecurableResource>();
    for (int i = 0; i < 16; ++i) {
      SecurableResourceId sresId = new SecurableResourceId("myTestDomain", "mySecResId_" + (resCount++));
      ResourceServiceLocal.getSingleton().registerResource(sresId, sysCtx);
      output.add(new MockSecurableResource(sresId));
    }
    return output;
  }

  List<ISecurableResource> testPasstroughFilter(@Context IAuthorizationContext ctx, List<ISecurableResource> input) {
    return input;
  }

    /**
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
  public void testInvokeTestNoResPasses() throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    Collection<ISecurableResource> testNoResPasses = testNoResPasses(context);
    assertTrue("All resources should be filtered out", testNoResPasses.isEmpty());
  }
}
