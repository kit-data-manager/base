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
package edu.kit.dama.rest.services.staging.test;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.rest.services.staging.test.util.TestAuthorizationContext;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.exceptions.PropertyNotFoundException;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationPersistenceImpl;
import edu.kit.dama.staging.handlers.impl.IngestPreparationHandler;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * @author jejkal
 */
@RunWith(Theories.class)
public class IngestPreparationTest {

    @DataPoints
    private static IngestInformationPersistenceImpl persistence = IngestInformationPersistenceImpl.getSingleton("Staging_Test");
    private static List<IngestInformation> entities = new LinkedList<IngestInformation>();
    private static IAuthorizationContext secCtx = new TestAuthorizationContext("someUser", "someGroup", Role.MEMBER);
    private StagingAccessPointConfiguration fileAccessPoint;
    private static final String tmpDir = "$tmp/cache/";//"$tmp/hallo"; //localDirectory.getAbsolutePath();
    private static final String tmpDirUrl = "file:///" + tmpDir;//"file:///" + tmpDir; //localDirectory.toURI().toString();
    private static boolean removeTempDir = false;
    private static File localDirectory = null;

    @BeforeClass
    public static void prepareTmpDir() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.WARNING, "Create staging directory");
        String localDir = replacePathVariableReplacements(tmpDir);
        localDirectory = new File(localDir);
        if (!localDirectory.exists()) {
            removeTempDir = localDirectory.mkdir();
        }
    }

    @AfterClass
    public static void removeTmpDir() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.WARNING, "Cleanup staging directory");
        if (removeTempDir) {
            try {
                delete(localDirectory);
            } catch (IOException ex) {
                Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Before
    public void prepare() {
        PU.setPersistenceUnitName("AuthorizationUnit-Test");
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Preparing test database");
        try {
            UserServiceLocal.getSingleton().register(new UserId("someUser"), Role.MANAGER, AuthorizationContext.factorySystemContext());
            GroupServiceLocal.getSingleton().create(new GroupId("someGroup"), new UserId("someUser"), AuthorizationContext.factorySystemContext());
        } catch (UnauthorizedAccessAttemptException | EntityNotFoundException | EntityAlreadyExistsException ex) {
            //this will never happen...if it does, try to continue and fail later
        }

        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Adding entries with context {0}", secCtx);

        for (int i = 0; i < 5; i++) {
            String uuid = UUID.randomUUID().toString();
            Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, " - Adding digital object {0}", uuid);
            IngestInformation entity = persistence.createEntity(new DigitalObjectId(uuid), secCtx);
            entities.add(entity);
        }
        Assert.assertEquals(5, entities.size());

        StagingConfigurationPersistence pers = StagingConfigurationPersistence.getSingleton("Staging_Test");
        fileAccessPoint = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
        fileAccessPoint.setImplementationClass(edu.kit.dama.staging.ap.impl.BasicStagingAccessPoint.class.getCanonicalName());
        fileAccessPoint.setRemoteBaseUrl(tmpDirUrl);
        fileAccessPoint.setLocalBasePath(tmpDir);
        fileAccessPoint.setDefaultAccessPoint(true);
        fileAccessPoint.setTransientAccessPoint(true);

        try {
            pers.saveAccessPointConfiguration(fileAccessPoint);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new RuntimeException("Failed to persist staging access point", ex);
        }
    }

    @After
    public void cleanup() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Cleanup");
        for (IngestInformation entity : entities) {
            persistence.removeEntity(entity.getId(), secCtx);
        }
        entities.clear();
    }

    @Test
    public void testConstruction() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing basic construction. Expecting a new instance of IngestPreparationHandler");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        Assert.assertNotNull(testCandidate);
    }

    @Test
    public void testNoProperties() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing ingest preparation without any properties. Expecting TransferPreparationException by AbstractTransferPreparationHandler");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        try {
            testCandidate.prepareTransfer(properties, secCtx);
            Assert.fail("Expected TransferPreparationException");
        } catch (TransferPreparationException tpe) {
            //success
        }
    }

    @Test
    public void testInvalidProviderClass() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing ingest preparation with invalid handler class. Expecting TransferPreparationException");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        try {
            testCandidate.prepareTransfer(properties, secCtx);
        } catch (TransferPreparationException tpe) {
            //ok
            return;
        }
        Assert.fail("Expected TransferPreparationException");
    }

    @Theory
    @Ignore
    public void testClientPreparation(String pHandlerId) {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing transfer client handler ID ''{0}''", pHandlerId);
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        try {
            testCandidate.prepareTransfer(properties, secCtx);
        } catch (TransferPreparationException tpe) {
            tpe.printStackTrace();
            Assert.fail("Caught TransferPreparationException");
        }
        IngestInformation preparedIngest = persistence.getEntityById(info.getId(), secCtx);
        Assert.assertNotNull(preparedIngest);
    }

    @Test
    public void testInvalidTransferClientPreparationHandler() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing ingest preparation throwing a TransferPreparationException during preparation. Expecting TransferPreparationException");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        try {
            testCandidate.prepareTransfer(properties, secCtx);
        } catch (TransferPreparationException tpe) {
            //ok
            return;
        }
        Assert.fail("Expected TransferPreparationException");
    }

    @Test(expected = PropertyNotFoundException.class)
    @Ignore
    public void testPropertyNotFoundExceptionDuringTransfer() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing ingest preparation throwing a PropertyNotFoundException during preparation. Expecting PropertyNotFoundException");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        properties.setStagingAccessPointId(fileAccessPoint.getUniqueIdentifier());
        try {
            testCandidate.prepareTransfer(properties, secCtx);
        } catch (TransferPreparationException tpe) {
            Assert.fail("Cause is no instance of PropertyNotFoundException");
        }
    }

    @Test
    public void testInvalidStagingAccessPoint() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing ingest preparation using invalid staging AccessPoint. Expecting IllegalArgumentException");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        properties.setStagingAccessPointId("thisIsInvalid");
        try {
            testCandidate.prepareTransfer(properties, secCtx);
            Assert.fail("Expected TransferPreparationException");
        } catch (TransferPreparationException tpe) {
            //check if wrong accessPointId is part of the error message...if not, this test fails.
            Assert.assertTrue(tpe.getMessage().contains("thisIsInvalid"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSecurityContext() {
        Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Testing ingest preparation without security context. Expecting IllegalArgumentException");
        IngestInformation info = getRandomEntity(secCtx);
        IngestPreparationHandler testCandidate = new IngestPreparationHandler(persistence, info);
        TransferClientProperties properties = new TransferClientProperties();
        properties.setStagingAccessPointId(fileAccessPoint.getUniqueIdentifier());
        try {
            testCandidate.prepareTransfer(properties, null);
        } catch (TransferPreparationException tpe) {
            Assert.fail("TransferPreparationException caught, IllegalArgumentException expected");
        }
    }

    /**
     * Helper method to get a random entity from the test database for the
     * provided context
     */
    private IngestInformation getRandomEntity(IAuthorizationContext pCtx) {
        int idx = (int) Math.floor(Math.random() * 5);
        idx = (idx > 4) ? 4 : idx;
        if (pCtx.equals(secCtx)) {
            return entities.get(idx);
        }
        return entities.get(idx);
    }

    /**
     * Replace available variables (currently only $tmp) within the provided
     * path string.
     *
     * @param pPathBefore The path read from the configuration
     *
     * @return The path including all replacements
     */
    private static String replacePathVariableReplacements(String pPathBefore) {
        String tmp = System.getProperty("java.io.tmpdir");
        if (!tmp.startsWith("/")) {
            tmp = "/" + tmp;
        }
        return pPathBefore.replaceAll(Pattern.quote("$tmp"), Matcher.quoteReplacement(tmp));
    }

    public static void delete(File file) throws IOException {
        if (file == null) {
            // nothing to do
            return;
        }
        if (file.isDirectory()) {
            //directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();
                Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Directory is deleted : "
                        + file.getAbsolutePath());
            } else {
                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
                    //recursive delete
                    delete(fileDelete);
                }
                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }
        } else {
            //if file, then delete it
            file.delete();
            Logger.getLogger(IngestPreparationTest.class.getName()).log(Level.INFO, "File is deleted : " + file.getAbsolutePath());
        }
    }
}
