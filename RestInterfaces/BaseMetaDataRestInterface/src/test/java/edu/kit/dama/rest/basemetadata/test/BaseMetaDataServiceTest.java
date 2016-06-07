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
package edu.kit.dama.rest.basemetadata.test;

import com.sun.jersey.test.framework.JerseyTest;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.ObjectViewMapping;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.basemetadata.types.MetadataSchemaWrapper;
import edu.kit.dama.rest.basemetadata.types.OrganizationUnitWrapper;
import edu.kit.dama.rest.basemetadata.types.ParticipantWrapper;
import edu.kit.dama.rest.basemetadata.types.RelationWrapper;
import edu.kit.dama.rest.basemetadata.types.TaskWrapper;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;

import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTransitionWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTypeWrapper;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.WebServiceException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author mf6319
 */
public class BaseMetaDataServiceTest extends JerseyTest {

    private static BaseMetaDataRestClient client;
    private static SimpleRESTContext ctx;

    public BaseMetaDataServiceTest() {
        super("edu.kit.dama.rest.basemetadata.test");
    }

    @Override
    protected int getPort(int defaultPort) {
        ServerSocket server = null;
        int port = -1;
        try {
            server = new ServerSocket(defaultPort);
            port = server.getLocalPort();
        } catch (IOException e) {
            // ignore
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if ((port != -1) || (defaultPort == 0)) {
            return port;
        }
        return getPort(0);
    }

    @BeforeClass
    public static void initClient() {
        ctx = new SimpleRESTContext("secret", "secret");
        client = new BaseMetaDataRestClient(
                "http://localhost:9998/BaseMetaDataTest", ctx);

        BaseMetaDataTestService.factoryDigitalObjectEntity(1l, false);
        BaseMetaDataTestService.factoryInvestigationEntity(1l, false);
        BaseMetaDataTestService.factoryMetadataSchemaEntity(1l, false);
        BaseMetaDataTestService.factoryOrganizationUnitEntity(1l, false);
        BaseMetaDataTestService.factoryParticipantEntity(1l, 1l, false);
        BaseMetaDataTestService.factoryRelationEntity(1l, 1l, false);
        BaseMetaDataTestService.factoryStudyEntity(1l, false);
        BaseMetaDataTestService.factoryTaskEntity(1l, false);
        BaseMetaDataTestService.factoryUserDataEntity(1l, false);
        BaseMetaDataTestService.factoryUserEntity(1l, false);
    }

    @Test
    public void testGetAllDigitalObjects1() {
        DigitalObjectWrapper wrapper = client.getAllDigitalObjects(0,
                Integer.MAX_VALUE);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"simple" object graph, so no label must be set
        assertEquals(null, wrapper.getEntities().get(0).getLabel()
        );
    }

    @Test
    public void testGetAllDigitalObjects2() {
        DigitalObjectWrapper wrapper = client.getAllDigitalObjects(0,
                Integer.MAX_VALUE, ctx);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"simple" object graph, so no label must be set
        assertEquals(null, wrapper.getEntities().get(0).getLabel());
    }

    @Test
    public void testGetAllDigitalObjectsByInvestigation1() {
        DigitalObjectWrapper wrapper = client.getAllDigitalObjects(1, 0,
                Integer.MAX_VALUE, Constants.USERS_GROUP_ID);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"simple" object graph, so no label must be set
        assertEquals(null, wrapper.getEntities().get(0).getLabel()
        );
    }

    @Test
    public void testGetAllDigitalObjectsByInvestigation2() {
        DigitalObjectWrapper wrapper = client.getAllDigitalObjects(1, 0,
                Integer.MAX_VALUE, Constants.USERS_GROUP_ID, ctx);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"simple" object graph, so no label must be set
        assertEquals(null, wrapper.getEntities().get(0).getLabel());
    }

    @Test
    public void testGetAllDigitalObjectsByDOI1() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectByDOI("12345",
                Constants.USERS_GROUP_ID);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"default" object graph, doi should be as provided before
        assertEquals("12345", wrapper.getEntities().get(0).
                getDigitalObjectIdentifier());
    }

    @Test
    public void testGetAllDigitalObjectsByDOI2() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectByDOI("12345",
                Constants.USERS_GROUP_ID, ctx);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"default" object graph, doi should be as provided before
        assertEquals("12345", wrapper.getEntities().get(0).
                getDigitalObjectIdentifier());
    }

    @Test
    public void testGetAllDigitalObjectsById1() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectById(1l);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"default" object graph, id should be as provided before
        assertEquals(1l, wrapper.getEntities().get(0).getBaseId().
                longValue());
    }

    @Test
    public void testGetAllDigitalObjectsById2() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectById(1l,
                ctx);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"default" object graph, id should be as provided before
        assertEquals(1l, wrapper.getEntities().get(0).getBaseId().
                longValue());
    }

    @Test
    public void testGetAllDigitalObjectsById3() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectById(1l,
                Constants.USERS_GROUP_ID, ctx);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        //"default" object graph, id should be as provided before
        assertEquals(1l, wrapper.getEntities().get(0).getBaseId().
                longValue());
    }

    @Test
    public void testGetAllDigitalObjectCount1() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectCount();
        //one result
        assertEquals(1, wrapper.getCount().intValue());
    }

    @Test
    public void testGetAllDigitalObjectCount2() {
        DigitalObjectWrapper wrapper = client.getDigitalObjectCount(Constants.USERS_GROUP_ID, ctx);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
    }

    @Test
    public void testGetOrganizationUnitCount() {
        OrganizationUnitWrapper wrapper = client.getOrganizationUnitCount(null,
                ctx);

        assertEquals(2, wrapper.getCount().intValue());
    }

    @Test
    public void testGetOrganizationUnits() {
        OrganizationUnitWrapper wrapper = client.getAllOrganizationUnits(
                0, Integer.MAX_VALUE, null, ctx);

        assertTrue(wrapper.getWrappedEntities().get(0).
                getOrganizationUnitId() == 1l);
        assertTrue(wrapper.getWrappedEntities().get(1).
                getOrganizationUnitId() == 2l);
    }

    @Test
    public void testGetOrganizationById() {
        OrganizationUnitWrapper wrapper = client.getOrganizationUnitById(
                1l, null, ctx);

        assertEquals(1, wrapper.getWrappedEntities().size());
        assertEquals("KIT", wrapper.getWrappedEntities().get(0).
                getOuName());
    }

    @Test
    public void testUpdateOrganizationUnit() {
        OrganizationUnit ou = BaseMetaDataTestService.
                factoryOrganizationUnitEntity(1l, false);
        ou.setAddress("test1");
        ou.setCity("test2");
        ou.setZipCode("12345");
        ou.setOrganizationUnitId(1l);
        ou.setOuName("test1");
        ou.setCountry("test2");
        ou.setWebsite("test3");

        OrganizationUnitWrapper wrapper = client.updateOrganizationUnit(
                1l, ou, null, ctx);

        assertEquals(1, wrapper.getWrappedEntities().size());
        assertTrue(ou.equals(wrapper.getWrappedEntities().get(0)));
    }

    @Test
    public void testGetMetadataSchemas() {
        MetadataSchemaWrapper msw = client.getAllMetadataSchemas(0,
                Integer.MAX_VALUE, null, ctx);

        assertEquals(msw.getCount().longValue(),
                msw.getWrappedEntities().size());
        assertEquals(2l, msw.getCount().longValue());
        assertEquals(2, msw.getWrappedEntities().size());
        assertEquals(1l, msw.getWrappedEntities().get(0).getId().
                longValue());
        assertEquals(2l, msw.getWrappedEntities().get(1).getId().
                longValue());
    }

    @Test
    public void createMetadataSchema() {
        // not yet, Kameraden... not yet
    }

    @Test
    public void testGetMetadataSchemaCount() {
        MetadataSchemaWrapper msw = client.getMetadataSchemaCount(null, ctx);
        assertEquals(2, msw.getCount().longValue());
    }

    @Test
    public void testGetMetadataSchemaById() {
        MetadataSchemaWrapper msw = client.getMetadataSchemaById(
                1l, null, ctx);

        assertEquals(1, msw.getWrappedEntities().size());
        assertEquals(1, msw.getWrappedEntities().get(0).getId().
                longValue());
    }

    @Test
    public void testGetTasks() {
        TaskWrapper tw = client.getAllTasks(0, Integer.MAX_VALUE, null, ctx);

        assertEquals(tw.getCount().longValue(),
                tw.getWrappedEntities().size());
        assertEquals(2l, tw.getCount().longValue());
        assertEquals(2, tw.getWrappedEntities().size());
        assertEquals(1l, tw.getWrappedEntities().get(0).getTaskId().
                longValue());
        assertEquals(2l, tw.getWrappedEntities().get(1).getTaskId().
                longValue());
    }

    @Test
    public void testCreateTask() {
        // not yet, Kameraden... not yet
    }

    @Test
    public void testGetTaskCount() {
        TaskWrapper tw = client.getTaskCount(null, ctx);

        assertEquals(2, tw.getCount().longValue());
    }

    @Test
    public void testGetTaskById() {
        TaskWrapper tw = client.getTaskById(1l, null, ctx);

        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(1, tw.getWrappedEntities().get(0).getTaskId().
                longValue());
    }

    @Test
    public void testGetUserDataEntities() {
        UserDataWrapper uw = client.getAllUserData(0, Integer.MAX_VALUE, null,
                ctx);

        assertEquals(uw.getCount().longValue(),
                uw.getWrappedEntities().size());
        assertEquals(2l, uw.getCount().longValue());
        assertEquals(2, uw.getWrappedEntities().size());
        assertEquals(1l, uw.getWrappedEntities().get(0).getUserId().
                longValue());
        assertEquals(2l, uw.getWrappedEntities().get(1).getUserId().
                longValue());
    }

    @Test
    public void testGetUserDataCount() {
        UserDataWrapper uw = client.getUserDataCount(null, ctx);

        assertEquals(2, uw.getCount().longValue());
    }

    @Test
    public void testGetUserDataById() {
        UserDataWrapper uw = client.getUserDataById(1l, null, ctx);

        assertEquals(1, uw.getWrappedEntities().size());
        assertEquals(1, uw.getWrappedEntities().get(0).getUserId().
                longValue());
    }

    @Test
    public void testGetParticipantById() {
        ParticipantWrapper pw = client.getParticipantById(1l, null, ctx);

        assertEquals(1, pw.getWrappedEntities().size());
        assertEquals(1, pw.getWrappedEntities().get(0).
                getUser().getUserId().longValue());
    }

    @Test
    public void testGetRelationById() {
        RelationWrapper rw = client.getRelationById(1l, null, ctx);

        assertEquals(1, rw.getWrappedEntities().size());
        assertEquals(1, rw.getWrappedEntities().get(0).
                getRelationId().longValue());
    }

    @Test
    public void testAddRelationToStudy() {
        RelationWrapper rw = client.getRelationById(1l, Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, rw.getWrappedEntities().size());
        StudyWrapper sw = client.addRelationToStudy(1l, rw.getEntities().get(0), Constants.USERS_GROUP_ID);
        assertEquals(1, sw.getWrappedEntities().size());
        assertEquals(2, sw.getWrappedEntities().get(0).getOrganizationUnits().size());
    }

    @Test
    public void testAddParticipantToInvestigation() {
        ParticipantWrapper pw = client.getParticipantById(1l, Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, pw.getWrappedEntities().size());
        InvestigationWrapper iw = client.addParticipantToInvestigation(1l, pw.getEntities().get(0), Constants.USERS_GROUP_ID);
        assertEquals(1, iw.getWrappedEntities().size());
        assertEquals(1, iw.getWrappedEntities().get(0).getParticipants().size());
    }

    @Test
    public void testAddMetadataSchemaToInvestigation() {
        MetadataSchemaWrapper sw = client.getMetadataSchemaById(1l, Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, sw.getWrappedEntities().size());
        InvestigationWrapper iw = client.addMetadataSchemaToInvestigation(1l, sw.getEntities().get(0), Constants.USERS_GROUP_ID);
        assertEquals(1, iw.getWrappedEntities().size());
        assertEquals(1, iw.getWrappedEntities().get(0).getMetaDataSchema().size());
    }

    @Test
    public void testAddDigitalObjectType() {
        DigitalObjectTypeWrapper tw = client.addDigitalObjectType(BaseMetaDataTestService.factoryObjectTypeEntity(1l, false), Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getWrappedEntities().size());
    }

    @Test
    public void testGetDigitalObjectTypeById() {
        DigitalObjectTypeWrapper tw = client.getDigitalObjectTypeById(1l, Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(Long.valueOf(1), tw.getWrappedEntities().get(0).getId());
    }

    @Test
    public void testGetDigitalObjectTypeCount() {
        DigitalObjectTypeWrapper tw = client.getDigitalObjectTypeCount(Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getCount().intValue());
    }

    @Test
    public void testAllDigitalObjectTypes() {
        DigitalObjectTypeWrapper tw = client.getAllDigitalObjectTypes(0, Integer.MAX_VALUE, Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(Long.valueOf(1), tw.getWrappedEntities().get(0).getId());
    }

    @Test
    public void testAddDigitalObjectTypeToDigitalObject() {
        DigitalObjectTypeWrapper tw = client.addDigitalObjectTypeToDigitalObject(1l, BaseMetaDataTestService.factoryObjectTypeEntity(1l, false), Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(Long.valueOf(1), tw.getWrappedEntities().get(0).getId());
    }

    @Test
    public void testGetDigitalObjectTypeByDigitalObject() {
        DigitalObjectTypeWrapper tw = client.getDigitalObjectTypesByDigitalObject(BaseMetaDataTestService.factoryDigitalObjectEntity(1l, false), Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(Long.valueOf(1), tw.getWrappedEntities().get(0).getId());
    }

    @Test
    public void testGetDigitalObjectsByDigitalObjectType() {
        DigitalObjectWrapper ow = client.getDigitalObjectByDigitalObjectType(BaseMetaDataTestService.factoryObjectTypeEntity(1l, false), Constants.USERS_GROUP_ID);
        assertEquals(1, ow.getWrappedEntities().size());
        assertEquals(1l, ow.getWrappedEntities().get(0).getBaseId().longValue());
    }

    @Test
    public void testGetDerivationInformation() {
        DigitalObjectTransitionWrapper tw = client.getDigitalObjectDerivationInformation(BaseMetaDataTestService.factoryDigitalObjectEntity(2l, false), Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, tw.getWrappedEntities().size());

        tw = client.getTransitionById(tw.getEntities().get(0).getId(), Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getEntities().get(0).getInputObjectViewMappings().size());
        assertEquals(1, tw.getEntities().get(0).getOutputObjects().size());
        assertEquals(2l, ((DigitalObject) tw.getEntities().get(0).getOutputObjects().toArray()[0]).getBaseId().longValue());
    }

    @Test
    public void testGetNoDerivationInformation() {
        DigitalObjectTransitionWrapper tw = client.getDigitalObjectDerivationInformation(BaseMetaDataTestService.factoryDigitalObjectEntity(666l, false), Constants.USERS_GROUP_ID, ctx);
        assertEquals(0, tw.getWrappedEntities().size());
    }

    @Test
    public void testGetContributionInformation() {
        DigitalObjectTransitionWrapper tw = client.getDigitalObjectDerivationInformation(BaseMetaDataTestService.factoryDigitalObjectEntity(2l, false), Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, tw.getWrappedEntities().size());

        tw = client.getTransitionById(tw.getEntities().get(0).getId(), Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getEntities().get(0).getInputObjectViewMappings().size());
        assertEquals(1, tw.getEntities().get(0).getOutputObjects().size());
        assertEquals(1l, ((ObjectViewMapping) tw.getEntities().get(0).getInputObjectViewMappings().toArray()[0]).getDigitalObject().getBaseId().longValue());
    }

    @Test
    public void testGetNoContributionInformation() {
        DigitalObjectTransitionWrapper tw = client.getDigitalObjectDerivationInformation(BaseMetaDataTestService.factoryDigitalObjectEntity(666l, false), Constants.USERS_GROUP_ID, ctx);
        assertEquals(0, tw.getWrappedEntities().size());
    }

    @Test
    public void testGetTransitionById() {
        DigitalObjectTransitionWrapper tw = client.getTransitionById(1l, Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(1l, tw.getEntities().get(0).getId().longValue());
        assertEquals(2l, ((DigitalObject) tw.getEntities().get(0).getOutputObjects().toArray()[0]).getBaseId().longValue());
        assertEquals(1l, ((ObjectViewMapping) tw.getEntities().get(0).getInputObjectViewMappings().toArray()[0]).getDigitalObject().getBaseId().longValue());
    }

    @Test
    public void testAddTransition() {
        DigitalObjectTransitionWrapper tw = client.addTransitionToDigitalObject(BaseMetaDataTestService.factoryDigitalObjectEntity(1l, false), BaseMetaDataTestService.factoryDigitalObjectEntity(2l, false), Constants.USERS_GROUP_ID);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(1l, tw.getEntities().get(0).getId().longValue());
        assertEquals(2l, ((DigitalObject) tw.getEntities().get(0).getOutputObjects().toArray()[0]).getBaseId().longValue());
        assertEquals(1l, ((ObjectViewMapping) tw.getEntities().get(0).getInputObjectViewMappings().toArray()[0]).getDigitalObject().getBaseId().longValue());
    }

    @Test
    public void testAddComplexTransition() {
        Map<DigitalObject, String> map = new HashMap<>();
        List<DigitalObject> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            DigitalObject o = new DigitalObject();
            o.setBaseId((long) i);
            map.put(o, "default");
            out.add(o);
        }
        JSONObject o = new JSONObject();
        o.putOnce("key", "value");
        DigitalObjectTransitionWrapper tw = client.addDigitalObjectTransition(map, out, TransitionType.ELASTICSEARCH, o.toString(), Constants.USERS_GROUP_ID, ctx);
        assertEquals(1, tw.getWrappedEntities().size());
        assertEquals(3, tw.getEntities().get(0).getOutputObjects().size());
        assertEquals(3, tw.getEntities().get(0).getInputObjectViewMappings().size());
    }

    @Test(expected = WebServiceException.class)
    public void testAddTransitionForInvalidId1() {
        client.addTransitionToDigitalObject(BaseMetaDataTestService.factoryDigitalObjectEntity(666l, false), BaseMetaDataTestService.factoryDigitalObjectEntity(2l, false), Constants.USERS_GROUP_ID);
        Assert.fail("HTTP 404 expected.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTransitionForInvalidId2() {
        DigitalObjectTransitionWrapper tw = client.addTransitionToDigitalObject(
                BaseMetaDataTestService.factoryDigitalObjectEntity(1l, false),
                BaseMetaDataTestService.factoryDigitalObjectEntity(2l, false),
                Constants.DEFAULT_VIEW,
                BaseMetaDataTestService.factoryDigitalObjectEntity(3l, false),
                Constants.USERS_GROUP_ID);
        Assert.fail("IllegalArgumentException expected.");
    }
}
