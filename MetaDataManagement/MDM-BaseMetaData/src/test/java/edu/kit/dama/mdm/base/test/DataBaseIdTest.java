/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.base.test;

import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import org.junit.BeforeClass;
import java.util.List;
import org.junit.AfterClass;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.base.UserData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author niedermaier
 */
@Ignore
public class DataBaseIdTest {

    /** For logging purposes. */
    private static Logger LOGGER = LoggerFactory.getLogger(UserTest.class);
    /** Manager holding all Information. */
    private static IMetaDataManager entityManager = null;

    @BeforeClass
    public static void prepareClass() {
      LOGGER.debug("Prepare DataBaseIdTest");
        MetaDataManagementHelper.replaceConfig(null);
        if (entityManager == null) {
            entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
        }
        /*
        testUser = createUserReference();
        us = createUserReference();
        referenceEntity = us;
        entityManager.save(referenceEntity);
        referenceEntity = testUser;
        entityManager.save(referenceEntity);*/
    }
    @AfterClass
    public static void releaseClass() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
      LOGGER.debug("Release DataBaseIdTest");
        List<UserData> all = entityManager.find(UserData.class);
        for (UserData item : all) {
            entityManager.remove(item);
        }
        entityManager.close();
    }

    @Test
    public void TestCreateDataBaseWithASpecifiedID() throws UnauthorizedAccessAttemptException {
        UserData study = new UserData();
        Long longNo = new Long(4711);
        study.setUserId(longNo);
        study = entityManager.save(study);
        assertNotSame(longNo, study.getUserId());
        assertTrue(entityManager.contains(study));
    }

    @Test
    public void TestCreateDataBaseWithAnExistID() {
        boolean exception = false;
        try {
            UserData user = new UserData();
            entityManager.save(user);
            assertTrue(entityManager.contains(user));

            UserData userNew = null;
            Long id = user.getUserId();
            userNew = entityManager.find(UserData.class, id);
            //try to change user id and save the instance
            // this should fail!
            userNew.setUserId(new Long(4711));
            entityManager.save(userNew);
            assertTrue(entityManager.contains(userNew));
            assertEquals(userNew.getUserId(), id);
            assertTrue(false);
        } catch (Exception e) {
            exception = true;
        }
        assertEquals(exception, true);
    }
}
