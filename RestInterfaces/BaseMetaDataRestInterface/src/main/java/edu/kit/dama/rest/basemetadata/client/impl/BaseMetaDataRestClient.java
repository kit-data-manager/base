/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.rest.basemetadata.client.impl;

import edu.kit.dama.rest.basemetadata.types.OrganizationUnitWrapper;
import edu.kit.dama.rest.basemetadata.types.MetadataSchemaWrapper;
import edu.kit.dama.rest.basemetadata.types.ParticipantWrapper;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.basemetadata.types.TaskWrapper;
import edu.kit.dama.rest.basemetadata.types.RelationWrapper;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.DigitalObjectType;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTransitionWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTypeWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Rest client for BaseMetadata.
 *
 * @author hartmann-v
 */
public final class BaseMetaDataRestClient extends AbstractRestClient {

    /**
     * The logger
     */
    // private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BaseMetaDataRestClient.class);
    //<editor-fold defaultstate="collapsed" desc="Parameter names">
    /**
     * The studyId.
     */
    private static final String QUERY_PARAMETER_STUDY_ID = "studyId";
    /**
     * The investigationId.
     */
    private static final String QUERY_PARAMETER_INVESTIGATION_ID
            = "investigationId";
    /**
     * Label of the digital object.
     */
    private static final String FORM_PARAMETER_DIGITAL_OBJECT_LABEL = "label";
    /**
     * uploader of the digital object.
     */
    private static final String FORM_PARAMETER_DIGITAL_OBJECT_UPLOADER_ID
            = "uploaderId";
    /**
     * Digital object id of a digital object
     */
    private static final String FORM_PARAMETER_DOI = "doi";
    /**
     * Topic of study and investigation
     */
    private static final String FORM_PARAMETER_TOPIC = "topic";
    /**
     * Note for investigation, study and digital object.
     */
    private static final String FORM_PARAMETER_NOTE = "note";
    /**
     * Start date of the investigation.
     */
    private static final String FORM_PARAMETER_START_DATE = "startDate";
    /**
     * End date of the investigation.
     */
    private static final String FORM_PARAMETER_END_DATE = "endDate";
    /**
     * Legal note for the study.
     */
    private static final String FORM_PARAMETER_STUDY_LEGAL_NOTE = "legalNote";
    /**
     * Id of the manager of a study/organization unit.
     */
    private static final String FORM_PARAMETER_MANAGER_USER_ID = "managerUserId";
    /**
     * The name of the organization unit.
     */
    private static final String FORM_PARAMETER_OU_NAME = "ouName";
    /**
     * The address of the organization unit.
     */
    private static final String FORM_PARAMETER_OU_ADDRESS = "address";
    /**
     * The zip code of the organization unit.
     */
    private static final String FORM_PARAMETER_OU_ZIP_CODE = "zipCode";
    /**
     * The city of the organization unit.
     */
    private static final String FORM_PARAMETER_OU_CITY = "city";
    /**
     * The country of the organization unit.
     */
    private static final String FORM_PARAMETER_OU_COUNTRY = "country";
    /**
     * The website of the organization unit.
     */
    private static final String FORM_PARAMETER_OU_WEBSITE = "website";
    /**
     * The metadata schema id.
     */
    private static final String FORM_PARAMETER_METADATA_SCHEMA_ID
            = "metadataSchemaId";
    /**
     * The metadata schema url.
     */
    private static final String FORM_PARAMETER_METADATA_SCHEMA_URL = "schemaUrl";
    /**
     * The task name.
     */
    private static final String FORM_PARAMETER_TASK_NAME = "taskName";
    /**
     * The userdata id of a participant.
     */
    private static final String FORM_PARAMETER_USERDATA_ID = "userDataId";
    /**
     * The task id of a participant.
     */
    private static final String FORM_PARAMETER_TASK_ID = "taskId";
    /**
     * The organization unit id of a relation.
     */
    private static final String FORM_PARAMETER_ORGANIZATION_UNIT_ID = "organizationUnitId";
    /**
     * The digital object type id.
     */
    private static final String FORM_PARAMETER_OBJECT_TYPE_ID = "objectTypeId";

    /**
     * The digital object type domain.
     */
    private static final String FORM_PARAMETER_OBJECT_TYPE_DOMAIN = "typeDomain";
    /**
     * The digital object type identifier.
     */
    private static final String FORM_PARAMETER_OBJECT_TYPE_IDENTIFIER = "identifier";
    /**
     * The digital object type version.
     */
    private static final String FORM_PARAMETER_OBJECT_TYPE_VERSION = "version";
    /**
     * The digital object type description.
     */
    private static final String FORM_PARAMETER_OBJECT_TYPE_DESCRIPTION = "description";
    /**
     * The second digital object id of a transition.
     */
    private static final String FORM_PARAMETER_OTHER_ID = "otherId";
    /**
     * The data organization view name of a transition's input.
     */
    private static final String FORM_PARAMETER_VIEW_NAME = "viewName";
    /**
     * The output digital object id of a transition.
     */
    private static final String FORM_PARAMETER_OUTPUT_ID = "outputId";
    /**
     * The transition type.
     */
    private static final String FORM_PARAMETER_TRANSITION_TYPE = "type";
    /**
     * The type data of a transition type.
     */
    private static final String FORM_PARAMETER_TRANSITION_TYPE_DATA = "typeData";
    /**
     * The form parameter containing a JSON representation of the input
     * object-view mappings of a transition.
     */
    private static final String FORM_PARAMETER_INPUT_OBJECT_MAP = "inputObjectMap";
    /**
     * The form parameter containing a JSON representation of the output object
     * list of a transition.
     */
    private static final String FORM_PARAMETER_OUTPUT_OBJECT_LIST = "outputObjectList";

//</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="URL components">
    /**
     * 'url' for count.
     */
    private static final String COUNT_URL = "/count";

    /**
     * Path for all digital objects.
     */
    private static final String DIGITAL_OBJECTS = "/digitalObjects";
    /**
     * Path to transitions.
     */
    private static final String DIGITAL_OBJECT_TRANSITIONS = "/transitions";

    /**
     * Path for digital object with given id.
     */
    private static final String DIGITAL_OBJECT_BY_ID = DIGITAL_OBJECTS + "/{0}";
    /**
     * Path for digital object with a given digital object id.
     */
    private static final String DIGITAL_OBJECT_BY_DOI = DIGITAL_OBJECTS + "/doi";
    /**
     * Path for the count of digital objects.
     */
    private static final String DIGITAL_OBJECTS_COUNT = DIGITAL_OBJECTS
            + COUNT_URL;

    /**
     * Path for the derivation information of a digital object.
     */
    private static final String DIGITAL_OBJECTS_DERIVATION_INFORMATION = DIGITAL_OBJECT_BY_ID
            + "/derivedFrom";
    /**
     * Path for the contribution information of a digital object.
     */
    private static final String DIGITAL_OBJECTS_CONTRIBUTION_INFORMATION = DIGITAL_OBJECT_BY_ID
            + "/contributesTo";

    /**
     * Path for adding transitions to a digital object.
     */
    private static final String DIGITAL_OBJECT_BY_ID_TRANSITION = DIGITAL_OBJECT_BY_ID
            + DIGITAL_OBJECT_TRANSITIONS;

    /**
     * Path to all investigations.
     */
    private static final String INVESTIGATIONS = "/investigations";
    /**
     * Path to investigation with given id.
     */
    private static final String INVESTIGATION_BY_ID = INVESTIGATIONS + "/{0}";
    /**
     * Path to digital objects of investigation given by id.
     */
    private static final String DIGITAL_OBJECT_BY_INVESTIGATION
            = INVESTIGATION_BY_ID + "/digitalObjects";

    /**
     * Path to transitions by id.
     */
    private static final String DIGITAL_OBJECT_TRANSITION_BY_ID = DIGITAL_OBJECT_TRANSITIONS + "/{0}";

    /**
     * Path to count digital objects of investigation given by id.
     */
    private static final String INVESTIGATIONS_COUNT = INVESTIGATIONS
            + COUNT_URL;
    /**
     * Path to studies.
     */
    private static final String STUDIES = "/studies";
    /**
     * Path to study with given id.
     */
    private static final String STUDY_BY_ID = STUDIES + "/{0}";
    /**
     * Path to investigation of study given by id.
     */
    private static final String INVESTIGATION_BY_STUDY = STUDY_BY_ID
            + "/investigations";
    /**
     * Path to relations of study given by id.
     */
    private static final String RELATIONS_BY_STUDY = STUDY_BY_ID + "/relations";

    /**
     * Path to count of all studies.
     */
    private static final String STUDIES_COUNT = STUDIES + COUNT_URL;
    /**
     * Path to organizationUnits.
     */
    private static final String ORGANIZATION_UNITS = "/organizationUnits";
    /**
     * Path to organizationUnits/count.
     */
    private static final String ORGANIZATION_UNITS_COUNT = ORGANIZATION_UNITS
            + COUNT_URL;
    /**
     * Path to organizationUnits with given id.
     */
    private static final String ORGANIZATION_UNIT_BY_ID = ORGANIZATION_UNITS
            + "/{0}";
    /**
     * Path to metadata schemas.
     */
    private static final String METADATA_SCHEMAS = "/metadataSchemas";
    /**
     * Path to metadata schemas of investigation given by id.
     */
    private static final String METADATA_SCHEMA_BY_INVESTIGATION
            = INVESTIGATION_BY_ID + METADATA_SCHEMAS;
    /**
     * Path to metadataSchemas/count.
     */
    private static final String METADATA_SCHEMAS_COUNT = METADATA_SCHEMAS
            + COUNT_URL;
    /**
     * Path to metadata schema with given id.
     */
    private static final String METADATA_SCHEMA_BY_ID = METADATA_SCHEMAS
            + "/{0}";
    /**
     * Path to tasks.
     */
    private static final String TASKS = "/tasks";
    /**
     * Path to tasks/count.
     */
    private static final String TASKS_COUNT = TASKS + COUNT_URL;
    /**
     * Path to task with given id.
     */
    private static final String TASK_BY_ID = TASKS + "/{0}";
    /**
     * Path to userData.
     */
    private static final String USERDATA = "/userData";
    /**
     * Path to userData/count.
     */
    private static final String USERDATA_COUNT = USERDATA + COUNT_URL;
    /**
     * Path to userdata with given id.
     */
    private static final String USERDATA_BY_ID = USERDATA + "/{0}";
    /**
     * Path to participants.
     */
    private static final String PARTICIPANTS = "/participants";
    /**
     * Path to participants of investigation given by id.
     */
    private static final String PARTICIPANTS_BY_INVESTIGATION
            = INVESTIGATION_BY_ID + PARTICIPANTS;
    /**
     * Path to participant with given id.
     */
    private static final String PARTICIPANT_BY_ID = PARTICIPANTS + "/{0}";
    /**
     * Path to experimenters.
     */
    private static final String EXPERIMENTERS = "/experimenters";
    /**
     * Path for experimenters for a digital object.
     */
    private static final String EXPERIMENTERS_BY_DIGITAL_OBJECT
            = DIGITAL_OBJECT_BY_ID + EXPERIMENTERS;

    /**
     * Path to relations.
     */
    private static final String RELATIONS = "/relations";
    /**
     * Path to relation with given id.
     */
    private static final String RELATION_BY_ID = RELATIONS + "/{0}";

    /**
     * Path to digital object types.
     */
    private static final String DIGITAL_OBJECT_TYPES = "/objectTypes";
    /**
     * Path to digital object types.
     */
    private static final String DIGITAL_OBJECT_TYPES_COUNT = DIGITAL_OBJECT_TYPES + COUNT_URL;
    /**
     * Path to digital object type with given id.
     */
    private static final String DIGITAL_OBJECT_TYPE_BY_ID = DIGITAL_OBJECT_TYPES + "/{0}";

    /**
     * Path to digital objects for the digital object types with the provided
     * id.
     */
    private static final String DIGITAL_OBJECTS_FOR_DIGITAL_OBJECT_TYPE = DIGITAL_OBJECT_TYPE_BY_ID + DIGITAL_OBJECTS;

    /**
     * Path to digital object types for the digital object with the provided id.
     */
    private static final String DIGITAL_OBJECT_TYPE_FOR_OBJECT = DIGITAL_OBJECT_BY_ID + DIGITAL_OBJECT_TYPES;

// </editor-fold>
    /**
     * Create a REST client with a predefined context.
     *
     * @param rootUrl root url of the staging service. (e.g.:
     * "http://dama.lsdf.kit.edu/KITDM/rest/basemetadata")
     * @param pContext initial context
     */
    public BaseMetaDataRestClient(String rootUrl, SimpleRESTContext pContext) {
        super(rootUrl, pContext);
    }

    // <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">
    /**
     * Perform a get for digital objects.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return DigitalObjectWrapper.
     */
    private DigitalObjectWrapper performDigitalObjectGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(DigitalObjectWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a put for digital objects.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return DigitalObjectWrapper.
     */
    private DigitalObjectWrapper performDigitalObjectPut(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(DigitalObjectWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for digital objects.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return DigitalObjectWrapper.
     */
    private DigitalObjectWrapper performDigitalObjectPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(DigitalObjectWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a delete for digital objects.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return DigitalObjectWrapper.
     */
//  private DigitalObjectWrapper performDigitalObjectDelete(String pPath, MultivaluedMap pQueryParams) {
//    return RestClientUtils.performDelete(DigitalObjectWrapper.class, getWebResource(pPath), pQueryParams);
//  }
    /**
     * Perform a get for investigations.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return StudiesWrapper.
     */
    private InvestigationWrapper performInvestigationGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(InvestigationWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a put for investigations.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return InvestigationWrapper.
     */
    private InvestigationWrapper performInvestigationPut(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(InvestigationWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for investigations.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return InvestigationWrapper.
     */
    private InvestigationWrapper performInvestigationPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(InvestigationWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a delete for investigations.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return InvestigationWrapper.
     */
//  private InvestigationWrapper performInvestigationDelete(String pPath, MultivaluedMap pQueryParams) {
//    return RestClientUtils.performDelete(InvestigationWrapper.class, getWebResource(pPath), pQueryParams);
//  }
    /**
     * Perform a get for studies.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return StudyWrapper.
     */
    private StudyWrapper performStudyGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(StudyWrapper.class, getWebResource(
                pPath), pQueryParams);
    }

    /**
     * Perform a put for studies.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return StudyWrapper.
     */
    private StudyWrapper performStudyPut(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(StudyWrapper.class, getWebResource(
                pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for studies.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return StudyWrapper.
     */
    private StudyWrapper performStudyPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(StudyWrapper.class, getWebResource(
                pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a delete for studies.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return StudyWrapper.
     */
//  private StudyWrapper performStudyDelete(String pPath, MultivaluedMap pQueryParams) {
//    return RestClientUtils.performDelete(StudyWrapper.class, getWebResource(pPath), pQueryParams);
//  }
    /**
     * Perform a get for organization unit.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return OrganizationUnitWrapper.
     */
    private OrganizationUnitWrapper performOrganizationUnitGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(OrganizationUnitWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a put for organization unit.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return OrganizationUnitWrapper.
     */
    private OrganizationUnitWrapper performOrganizationUnitPut(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(OrganizationUnitWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for organization unit.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return OrganizationUnitWrapper.
     */
    private OrganizationUnitWrapper performOrganizationUnitPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(OrganizationUnitWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a get for metadata schema.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return MetadataSchemaWrapper.
     */
    private MetadataSchemaWrapper performMetadataSchemaGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(MetadataSchemaWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a post for metadata schema.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return MetadataSchemaWrapper.
     */
    private MetadataSchemaWrapper performMetadataSchemaPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(MetadataSchemaWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a get for task.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return TaskWrapper.
     */
    private TaskWrapper performTaskGet(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(TaskWrapper.class, getWebResource(
                pPath), pQueryParams);
    }

    /**
     * Perform a post for task.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return TaskWrapper.
     */
    private TaskWrapper performTaskPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(TaskWrapper.class, getWebResource(
                pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a get for userData.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return UserDataWrapper.
     */
    private UserDataWrapper performUserDataGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(UserDataWrapper.class, getWebResource(
                pPath), pQueryParams);
    }

    /**
     * Perform a put for userData.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return UserDataWrapper.
     */
//  private UserDataWrapper performUserDataPut(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
//    return RestClientUtils.performPut(UserDataWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
//  }
    /**
     * Perform a post for userData.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return UserDataWrapper.
     */
//  private UserDataWrapper performUserDataPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
//    return RestClientUtils.performPost(UserDataWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
//  }
    /**
     * Perform a get for participant.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return ParticipantWrapper.
     */
    private ParticipantWrapper performParticipantGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(ParticipantWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a get for object transitions.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return DigitalObjectTransitionWrapper.
     */
    private DigitalObjectTransitionWrapper performDigitalObjectTransitionGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(DigitalObjectTransitionWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a post for object transitions.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams form parameters
     * @return DigitalObjectTransitionWrapper.
     */
    private DigitalObjectTransitionWrapper performDigitalObjectTransitionPost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(DigitalObjectTransitionWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for participant.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return ParticipantWrapper.
     */
//  private ParticipantWrapper performParticipantPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
//    return RestClientUtils.performPost(ParticipantWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
//  }
    /**
     * Perform a get for relation.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return RelationWrapper.
     */
    private RelationWrapper performRelationGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(RelationWrapper.class, getWebResource(
                pPath), pQueryParams);
    }

    /**
     * Perform a put for relation.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return RelationWrapper.
     */
//  private RelationWrapper performRelationPut(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
//    return RestClientUtils.performPut(RelationWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
//  }
    /**
     * Perform a post for relation.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return RelationWrapper.
     */
//  private RelationWrapper performRelationPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
//    return RestClientUtils.performPost(RelationWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
//  }
    /**
     * Perform a get for digital object type.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return DigitalObjectTypeWrapper.
     */
    private DigitalObjectTypeWrapper performDigitalObjectTypeGet(String pPath,
            MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(DigitalObjectTypeWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a post for digital object type.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return DigitalObjectTypeWrapper.
     */
    private DigitalObjectTypeWrapper performDigitalObjectTypePost(String pPath,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(DigitalObjectTypeWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    // </editor-fold>
    //<editor-fold defaultstate="collapsed" desc="getAll[OrganizationUnits|MetadataSchemas|Tasks|UserData|Studies|Investigations|DigitalObjects|DigitalObjectTypes]">
    /**
     * Get all OrganizationUnits.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId e.g. USERS
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper getAllOrganizationUnits(int pFirstIndex,
            int pResults, String pGroupId) {
        return getAllOrganizationUnits(pFirstIndex, pResults, pGroupId, null);
    }

    /**
     * Get all OrganizationUnits.
     *
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @param pGroupId groupId e.g. USERS
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper getAllOrganizationUnits(int pFirstIndex,
            int pResults, String pGroupId, SimpleRESTContext pSecurityContext) {
        OrganizationUnitWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue
                = performOrganizationUnitGet(ORGANIZATION_UNITS, queryParams);
        return returnValue;
    }

    /**
     * Get all metadata schemas.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId e.g. USERS
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper getAllMetadataSchemas(int pFirstIndex,
            int pResults, String pGroupId) {
        return getAllMetadataSchemas(pFirstIndex, pResults, pGroupId, null);
    }

    /**
     * Get all metadata schemas.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId e.g. USERS
     * @param pSecurityContext security context
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper getAllMetadataSchemas(int pFirstIndex,
            int pResults, String pGroupId, SimpleRESTContext pSecurityContext) {
        MetadataSchemaWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performMetadataSchemaGet(METADATA_SCHEMAS, queryParams);
        return returnValue;
    }

    /**
     * Get all tasks.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId e.g. USERS
     *
     * @return TaskWrapper
     */
    public TaskWrapper getAllTasks(int pFirstIndex, int pResults,
            String pGroupId) {
        return getAllTasks(pFirstIndex, pResults, pGroupId, null);
    }

    /**
     * Get all tasks.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId e.g. USERS
     * @param pSecurityContext security context
     *
     * @return TaskWrapper
     */
    public TaskWrapper getAllTasks(int pFirstIndex, int pResults,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        TaskWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performTaskGet(TASKS, queryParams);
        return returnValue;
    }

    /**
     * Get all UserData.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId e.g. USERS
     *
     * @return UserDataWrapper
     */
    public UserDataWrapper getAllUserData(int pFirstIndex, int pResults,
            String pGroupId) {
        return getAllUserData(pFirstIndex, pResults, pGroupId, null);
    }

    /**
     * Get all UserData.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @param pGroupId groupId e.g. USERS
     *
     * @return UserDataWrapper
     */
    public UserDataWrapper getAllUserData(int pFirstIndex, int pResults,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performUserDataGet(USERDATA, queryParams);
        return returnValue;
    }

    /**
     * Get all studies.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId ( e.g. USERS)
     *
     * @return StudyWrapper
     */
    public StudyWrapper getAllStudies(int pFirstIndex, int pResults,
            String pGroupId) {
        return getAllStudies(pFirstIndex, pResults, pGroupId, null);
    }

    /**
     * Get all studies.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId ( e.g. USERS)
     * @param pSecurityContext security context
     *
     * @return StudyWrapper
     */
    public StudyWrapper getAllStudies(int pFirstIndex, int pResults,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        StudyWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performStudyGet(STUDIES, queryParams);
        return returnValue;
    }

    /**
     * Get all investigations.
     *
     * @param pStudyId studyId (optional)
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId ( e.g. USERS)
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper getAllInvestigations(long pStudyId,
            int pFirstIndex, int pResults, String pGroupId) {
        return getAllInvestigations(pStudyId, pFirstIndex, pResults, pGroupId,
                null);
    }

    /**
     * Get all investigations.
     *
     * @param pStudyId studyId (optional, pStudyId &lt; 0 == ignored)
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId ( e.g. USERS)
     * @param pSecurityContext security context
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper getAllInvestigations(long pStudyId,
            int pFirstIndex, int pResults, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        InvestigationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        if (pStudyId > 0) {
            queryParams.add(QUERY_PARAMETER_STUDY_ID, Long.toString(pStudyId));
        }

        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performInvestigationGet(INVESTIGATIONS, queryParams);
        return returnValue;
    }

    /**
     * Get all digital objects of special group and/or investigation.
     *
     * @param pInvestigationId filter results by given investigation (e.g.: -1)
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getAllDigitalObjects(long pInvestigationId,
            int pFirstIndex, int pResults, String pGroupId) {
        return getAllDigitalObjects(pInvestigationId, pFirstIndex, pResults,
                pGroupId, null);
    }

    /**
     * Get all digital objects of special group and/or investigation.
     *
     * @param pInvestigationId filter results by given investigation (e.g.: -1)
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getAllDigitalObjects(long pInvestigationId,
            int pFirstIndex, int pResults, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        if (pInvestigationId > 0) {
            queryParams.add(QUERY_PARAMETER_INVESTIGATION_ID, Long.toString(
                    pInvestigationId));
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performDigitalObjectGet(DIGITAL_OBJECTS, queryParams);
        return returnValue;
    }

    /**
     * Get all digital objects.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getAllDigitalObjects(int pFirstIndex,
            int pResults) {
        return getAllDigitalObjects(-1, pFirstIndex, pResults, null, null);
    }

    /**
     * Get all digital objects.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getAllDigitalObjects(int pFirstIndex,
            int pResults, SimpleRESTContext pSecurityContext) {
        return getAllDigitalObjects(-1, pFirstIndex, pResults, null,
                pSecurityContext);
    }

    /**
     * Get all digital object types.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectTypeWrapper getAllDigitalObjectTypes(int pFirstIndex, int pResults, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        DigitalObjectTypeWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performDigitalObjectTypeGet(DIGITAL_OBJECTS, queryParams);
        return returnValue;
    }

    /**
     * Get all digital object types.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectTypeWrapper getAllDigitalObjectTypes(int pFirstIndex, int pResults, String pGroupId) {
        DigitalObjectTypeWrapper returnValue;
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));
        returnValue = performDigitalObjectTypeGet(DIGITAL_OBJECT_TYPES, queryParams);
        return returnValue;
    }

    /**
     * Get all digital object types of the provided digital object.
     *
     * @param pDigitalObject The digital object.
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypesByDigitalObject(DigitalObject pDigitalObject, String pGroupId) {
        return getDigitalObjectTypesByDigitalObject(pDigitalObject, pGroupId, null);
    }

    /**
     * Get all digital object types of the provided digital object.
     *
     * @param pDigitalObject The digital object.
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypesByDigitalObject(DigitalObject pDigitalObject, String pGroupId, SimpleRESTContext pSecurityContext) {
        DigitalObjectTypeWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (pDigitalObject == null) {
            throw new IllegalArgumentException(
                    "Argument pDigitalObject must not be null");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDigitalObjectTypeGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_TYPE_FOR_OBJECT, pDigitalObject.getBaseId()), queryParams);
        return returnValue;
    }

    /**
     * Get the digital object transition(s) the provided digital object is
     * derived from. Typically, only one transition should contribute to a
     * digital object, but this may change in future.
     *
     * @param pDigitalObject The digital object.
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTransitionWrapper getDigitalObjectDerivationInformation(DigitalObject pDigitalObject, String pGroupId) {
        return getDigitalObjectDerivationInformation(pDigitalObject, pGroupId, null);
    }

    /**
     * Get the digital object transition(s) the provided digital object is
     * derived from. Typically, only one transition should contribute to a
     * digital object, but this may change in future.
     *
     * @param pDigitalObject The digital object.
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper getDigitalObjectDerivationInformation(DigitalObject pDigitalObject, String pGroupId, SimpleRESTContext pSecurityContext) {
        DigitalObjectTransitionWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (pDigitalObject == null) {
            throw new IllegalArgumentException(
                    "Argument pDigitalObject must not be null");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDigitalObjectTransitionGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECTS_DERIVATION_INFORMATION, pDigitalObject.getBaseId()), queryParams);
        return returnValue;
    }

    /**
     * Get the digital object transition(s) the provided digital object is
     * contributing to as an input.
     *
     * @param pDigitalObject The digital object.
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTransitionWrapper getDigitalObjectContributionInformation(DigitalObject pDigitalObject, String pGroupId) {
        return getDigitalObjectContributionInformation(pDigitalObject, pGroupId, null);
    }

    /**
     * Get the digital object transition(s) the provided digital object is
     * contributing to as an input.
     *
     * @param pDigitalObject The digital object.
     * @param pGroupId groupId filter results by given group (e.g.: USERS)
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper getDigitalObjectContributionInformation(DigitalObject pDigitalObject, String pGroupId, SimpleRESTContext pSecurityContext) {
        DigitalObjectTransitionWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (pDigitalObject == null) {
            throw new IllegalArgumentException(
                    "Argument pDigitalObject must not be null");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDigitalObjectTransitionGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECTS_CONTRIBUTION_INFORMATION, pDigitalObject.getBaseId()), queryParams);
        return returnValue;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[OrganizationUnit|MetadataSchema|Task|UserData|Participant|Relation|Study|Investigation|DigitalObject|DigitalObjectTransition]ById">
    /**
     * Get digital object transition with given id.
     *
     * @param pTransitionId id of the transition.
     * @param pGroupId groupId the transition belongs to e.g. USERS
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper getTransitionById(long pTransitionId, String pGroupId) {
        return getTransitionById(pTransitionId, pGroupId, null);
    }

    /**
     * Get digital object transition with given id.
     *
     * @param pTransitionId id of the transition.
     * @param pGroupId groupId the transition belongs to e.g. USERS
     * @param pSecurityContext security context.
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper getTransitionById(long pTransitionId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pTransitionId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pTransitionId must be larger than 0");
        }
        DigitalObjectTransitionWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performDigitalObjectTransitionGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_TRANSITION_BY_ID, pTransitionId), queryParams);
        return returnValue;
    }

    /**
     * Get relation with given id.
     *
     * @param pRelationId id of the relation.
     * @param pGroupId groupId the relation belongs to e.g. USERS
     *
     * @return RelationWrapper
     */
    public RelationWrapper getRelationById(long pRelationId, String pGroupId) {
        return getRelationById(pRelationId, pGroupId, null);
    }

    /**
     * Get relation with given id.
     *
     * @param pRelationId id of the relation.
     * @param pGroupId groupId the relation belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return RelationWrapper
     */
    public RelationWrapper getRelationById(long pRelationId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pRelationId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pRelationId must be larger than 0");
        }
        RelationWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performRelationGet(RestClientUtils.encodeUrl(
                RELATION_BY_ID, pRelationId), queryParams);
        return returnValue;
    }

    /**
     * Get participant with given id.
     *
     * @param pParticipantId id of the participant.
     * @param pGroupId groupId the participant belongs to e.g. USERS.
     *
     * @return ParticipantWrapper
     */
    public ParticipantWrapper getParticipantById(long pParticipantId,
            String pGroupId) {
        return getParticipantById(pParticipantId, pGroupId, null);
    }

    /**
     * Get participant with given id.
     *
     * @param pParticipantId id of the participant.
     * @param pGroupId groupId the participant belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return ParticipantWrapper
     */
    public ParticipantWrapper getParticipantById(long pParticipantId,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        if (pParticipantId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pParticipantId must be larger than 0");
        }
        ParticipantWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performParticipantGet(RestClientUtils.encodeUrl(
                PARTICIPANT_BY_ID, pParticipantId), queryParams);
        return returnValue;
    }

    /**
     * Get userData with given id.
     *
     * @param pUserDataId id of the userData.
     * @param pGroupId groupId the userData belongs to e.g. USERS.
     *
     * @return UserDataWrapper
     */
    public UserDataWrapper getUserDataById(long pUserDataId, String pGroupId) {
        return getUserDataById(pUserDataId, pGroupId, null);
    }

    /**
     * Get userData with given id.
     *
     * @param pUserDataId id of the userData.
     * @param pGroupId groupId the userData belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return UserDataWrapper
     */
    public UserDataWrapper getUserDataById(long pUserDataId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pUserDataId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pUserDataId must be larger than 0");
        }
        UserDataWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performUserDataGet(RestClientUtils.encodeUrl(
                USERDATA_BY_ID, pUserDataId), queryParams);
        return returnValue;
    }

    /**
     * Get task with given id.
     *
     * @param pTaskId id of the task.
     * @param pGroupId groupId the task belongs to e.g. USERS.
     *
     * @return TaskWrapper
     */
    public TaskWrapper getTaskById(long pTaskId, String pGroupId) {
        return getTaskById(pTaskId, pGroupId, null);
    }

    /**
     * Get task with given id.
     *
     * @param pTaskId id of the task.
     * @param pGroupId groupId the task belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return TaskWrapper
     */
    public TaskWrapper getTaskById(long pTaskId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pTaskId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pTaskId must be larger than 0");
        }
        TaskWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performTaskGet(RestClientUtils.encodeUrl(TASK_BY_ID,
                pTaskId), queryParams);
        return returnValue;
    }

    /**
     * Get metadataSchema with given id.
     *
     * @param pMetadataSchemaId id of the metadataSchema.
     * @param pGroupId groupId the metadataSchema belongs to e.g. USERS.
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper getMetadataSchemaById(long pMetadataSchemaId,
            String pGroupId) {
        return getMetadataSchemaById(pMetadataSchemaId, pGroupId, null);
    }

    /**
     * Get metadataSchema with given id.
     *
     * @param pMetadataSchemaId id of the metadataSchema.
     * @param pGroupId groupId the metadataSchema belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper getMetadataSchemaById(long pMetadataSchemaId,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        if (pMetadataSchemaId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pMetadataSchemaId must be larger than 0");
        }
        MetadataSchemaWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performMetadataSchemaGet(RestClientUtils.encodeUrl(
                METADATA_SCHEMA_BY_ID, pMetadataSchemaId), queryParams);
        return returnValue;
    }

    /**
     * Get organizationUnit with given id.
     *
     * @param pOrganizationUnitId id of the organizationUnit.
     * @param pGroupId groupId the organizationUnit belongs to e.g. USERS.
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper getOrganizationUnitById(
            long pOrganizationUnitId, String pGroupId) {
        return getOrganizationUnitById(pOrganizationUnitId, pGroupId, null);
    }

    /**
     * Get organizationUnit with given id.
     *
     * @param pOrganizationUnitId id of the organizationUnit.
     * @param pGroupId groupId the organizationUnit belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper getOrganizationUnitById(
            long pOrganizationUnitId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pOrganizationUnitId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pOrganizationUnitId must be larger than 0");
        }
        OrganizationUnitWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performOrganizationUnitGet(RestClientUtils.encodeUrl(
                ORGANIZATION_UNIT_BY_ID, pOrganizationUnitId), queryParams);
        return returnValue;
    }

    /**
     * Get study with given id.
     *
     * @param pStudyId id of the study.
     * @param pGroupId groupId the study belongs to e.g. USERS.
     *
     * @return StudyWrapper
     */
    public StudyWrapper getStudyById(long pStudyId, String pGroupId) {
        return getStudyById(pStudyId, pGroupId, null);
    }

    /**
     * Get study with given id.
     *
     * @param pStudyId id of the study.
     * @param pGroupId groupId the study belongs to e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return StudyWrapper
     */
    public StudyWrapper getStudyById(long pStudyId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pStudyId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pStudyId must be larger than 0");
        }
        StudyWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performStudyGet(RestClientUtils.encodeUrl(STUDY_BY_ID,
                pStudyId), queryParams);
        return returnValue;
    }

    /**
     * Get investigation with given id.
     *
     * @param pInvestigationId id of the investigation
     * @param pGroupId groupId the investigation belongs to e.g. USERS
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper getInvestigationById(long pInvestigationId,
            String pGroupId) {
        return getInvestigationById(pInvestigationId, pGroupId, null);
    }

    /**
     * Get investigation with given id.
     *
     * @param pInvestigationId id of the investigation
     * @param pGroupId groupId the investigation belongs to e.g. USERS
     * @param pSecurityContext security context
     * @return InvestigationWrapper
     */
    public InvestigationWrapper getInvestigationById(long pInvestigationId,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        if (pInvestigationId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pInvestigationId must be larger than 0");
        }
        InvestigationWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performInvestigationGet(RestClientUtils.encodeUrl(
                INVESTIGATION_BY_ID, pInvestigationId), queryParams);
        return returnValue;
    }

    /**
     * Get digital object with given id.
     *
     * @param pId id of the digital object
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectById(long pId, String pGroupId) {
        return getDigitalObjectById(pId, pGroupId, null);
    }

    /**
     * Get digital object with given id.
     *
     * @param pId id of the digital object
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     * @param pSecurityContext security context
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectById(long pId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pId must be larger than 0");
        }
        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performDigitalObjectGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_BY_ID, pId), queryParams);
        return returnValue;
    }

    /**
     * Get all digital objects that have the provided digital object type
     * assigned.
     *
     * @param pType The digital object type.
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectByDigitalObjectType(DigitalObjectType pType, String pGroupId) {
        return getDigitalObjectByDigitalObjectType(pType, pGroupId, null);
    }

    /**
     * Get all digital objects that have the provided digital object type
     * assigned.
     *
     * @param pType The digital object type.
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     * @param pSecurityContext security context
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectByDigitalObjectType(DigitalObjectType pType, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pType == null) {
            throw new IllegalArgumentException(
                    "Argument pType must not be null.");
        }
        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performDigitalObjectGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECTS_FOR_DIGITAL_OBJECT_TYPE, pType.getId()), queryParams);
        return returnValue;
    }

    /**
     * Get digital object type with given id.
     *
     * @param pId id of the digital object type.
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypeById(long pId, String pGroupId) {
        return getDigitalObjectTypeById(pId, pGroupId, null);
    }

    /**
     * Get digital object type with the given id.
     *
     * @param pId id of the digital object type.
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypeById(long pId, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pId must be larger than 0");
        }
        DigitalObjectTypeWrapper returnValue;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        returnValue = performDigitalObjectTypeGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_TYPE_BY_ID, pId), queryParams);
        return returnValue;
    }

    /**
     * Get digital object with given id.
     *
     * @param pDigitalObjectId id of the digital object
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectByDOI(String pDigitalObjectId,
            String pGroupId) {
        return getDigitalObjectByDOI(pDigitalObjectId, pGroupId, null);
    }

    /**
     * Get digital object with given id.
     *
     * @param pDigitalObjectId id of the digital object
     * @param pGroupId id of the group of the user (for authorization check only
     * e.g. USERS).
     * @param pSecurityContext security context
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectByDOI(String pDigitalObjectId,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        if (pDigitalObjectId == null) {
            throw new IllegalArgumentException(
                    "Argument pDigitalObjectId must not be null");
        }
        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(FORM_PARAMETER_DOI, pDigitalObjectId);
        returnValue = performDigitalObjectGet(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_BY_DOI), queryParams);
        return returnValue;
    }

    /**
     * Get digital object with given id.
     *
     * @param pDigitalObjectId id of the digital object
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectById(long pDigitalObjectId) {
        return getDigitalObjectById(pDigitalObjectId, null, null);
    }

    /**
     * Get digital object with given id.
     *
     * @param pDigitalObjectId id of the digital object
     * @param pSecurityContext security context
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectById(long pDigitalObjectId,
            SimpleRESTContext pSecurityContext) {
        return getDigitalObjectById(pDigitalObjectId, null, pSecurityContext);
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[OrganizationUnit|MetadataSchema|Task|UserData|Study|Investigation|DigitalObject]Count">
    /**
     * Get no of all organization units.
     *
     * @param pGroupId groupId the organization unit belongs to e.g. USERS.
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper getOrganizationUnitCount(String pGroupId) {
        return getOrganizationUnitCount(pGroupId, null);
    }

    /**
     * Get no of all organization units.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId the organization units belong to e.g. USERS.
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper getOrganizationUnitCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams = null;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        return performOrganizationUnitGet(ORGANIZATION_UNITS_COUNT, queryParams);
    }

    /**
     * Get no of all metadata schema.
     *
     * @param pGroupId groupId the metadata schema belongs to e.g. USERS.
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper getMetadataSchemaCount(String pGroupId) {
        return getMetadataSchemaCount(pGroupId, null);
    }

    /**
     * Get no of all metadata schema.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId the metadata schema belongs to e.g. USERS.
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper getMetadataSchemaCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams = null;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        return performMetadataSchemaGet(METADATA_SCHEMAS_COUNT, queryParams);
    }

    /**
     * Get no of all task.
     *
     * @param pGroupId groupId the task belongs to e.g. USERS.
     *
     * @return TaskWrapper
     */
    public TaskWrapper getTaskCount(String pGroupId) {
        return getTaskCount(pGroupId, null);
    }

    /**
     * Get no of all task.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId the task belongs to e.g. USERS.
     * @return TaskWrapper
     */
    public TaskWrapper getTaskCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams = null;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        return performTaskGet(TASKS_COUNT, queryParams);
    }

    /**
     * Get no of all userData.
     *
     * @param pGroupId groupId the userData belongs to e.g. USERS.
     *
     * @return UserDataWrapper
     */
    public UserDataWrapper getUserDataCount(String pGroupId) {
        return getUserDataCount(pGroupId, null);
    }

    /**
     * Get no of all userData.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId the userData belongs to e.g. USERS.
     * @return UserDataWrapper
     */
    public UserDataWrapper getUserDataCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams = null;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        return performUserDataGet(USERDATA_COUNT, queryParams);
    }

    /**
     * Get no of all studies.
     *
     * @param pGroupId groupId the study belongs to e.g. USERS.
     *
     * @return StudyWrapper
     */
    public StudyWrapper getStudyCount(String pGroupId) {
        return getStudyCount(pGroupId, null);
    }

    /**
     * Get no of all studies.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId the study belongs to e.g. USERS.
     * @return StudyWrapper
     */
    public StudyWrapper getStudyCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams = null;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        return performStudyGet(STUDIES_COUNT, queryParams);
    }

    /**
     * Get no of all studies in group 'USERS'.
     *
     * @return StudyWrapper
     */
    public StudyWrapper getStudyCount() {
        return getStudyCount((SimpleRESTContext) null);
    }

    /**
     * Get no of all studies in group 'USERS'.
     *
     * @param pSecurityContext security context
     * @return StudyWrapper
     */
    public StudyWrapper getStudyCount(SimpleRESTContext pSecurityContext) {
        return getStudyCount(Constants.USERS_GROUP_ID, pSecurityContext);
    }

    /**
     * Get no of investigations (of group).
     *
     * @param pStudyId studyId (optional, if null all investigation will be
     * @param pGroupId groupId ( e.g. USERS) returned.)
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper getInvestigationCount(long pStudyId,
            String pGroupId) {
        return getInvestigationCount(pStudyId, pGroupId, null);
    }

    /**
     * Get no of investigations (of group).
     *
     *
     * @param pStudyId studyId (optional, if smaller 1 all investigation will be
     * returned.)
     * @param pGroupId groupId ( e.g. USERS)
     * @param pSecurityContext security context
     * @return InvestigationWrapper
     */
    public InvestigationWrapper getInvestigationCount(long pStudyId,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        if (pStudyId > 0) {
            queryParams.add(QUERY_PARAMETER_STUDY_ID, Long.toString(pStudyId));
        }
        if (queryParams.isEmpty()) {
            queryParams = null;
        }
        return performInvestigationGet(INVESTIGATIONS_COUNT, queryParams);
    }

    /**
     * Get no of all digital objects.
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectCount() {
        return getDigitalObjectCount(Constants.USERS_GROUP_ID, null);
    }

    /**
     * Get no of all digital objects.
     *
     * @param pGroupId groupId ( e.g. USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectCount(String pGroupId) {
        return getDigitalObjectCount(pGroupId, null);
    }

    /**
     * Get no of all digital objects.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId ( e.g. USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper getDigitalObjectCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        return performDigitalObjectGet(DIGITAL_OBJECTS_COUNT, queryParams);
    }

    /**
     * Get no of all digital object types.
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypeCount() {
        return getDigitalObjectTypeCount(Constants.USERS_GROUP_ID);
    }

    /**
     * Get no of all digital object types.
     *
     * @param pGroupId groupId ( e.g. USERS)
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypeCount(String pGroupId) {
        return getDigitalObjectTypeCount(pGroupId, null);
    }

    /**
     * Get no of all digital object types.
     *
     * @param pSecurityContext security context
     * @param pGroupId groupId ( e.g. USERS)
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectTypeWrapper getDigitalObjectTypeCount(
            String pGroupId, SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        return performDigitalObjectTypeGet(DIGITAL_OBJECT_TYPES_COUNT, null);
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="update[OrganizationUnit|Study|Investigation|DigitalObject]">
    /**
     * Update the organizationUnit with given id.
     *
     * @param pOrganizationUnitId id of the organization unit.
     * @param pOrganizationUnit Organization unit with updated values.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper updateOrganizationUnit(
            long pOrganizationUnitId, OrganizationUnit pOrganizationUnit,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        if (pOrganizationUnitId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pOrganizationUnitId must be larger than 0");
        }
        if (pOrganizationUnit == null) {
            throw new IllegalArgumentException(
                    "Argument pOrganizationUnit must not be null");
        }

        OrganizationUnitWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_OU_NAME, pOrganizationUnit.getOuName());
        formParams.
                add(FORM_PARAMETER_OU_ADDRESS, pOrganizationUnit.getAddress());
        formParams.add(FORM_PARAMETER_OU_CITY, pOrganizationUnit.getCity());
        formParams.add(FORM_PARAMETER_OU_ZIP_CODE, pOrganizationUnit.
                getZipCode());
        formParams.
                add(FORM_PARAMETER_OU_COUNTRY, pOrganizationUnit.getCountry());
        formParams.
                add(FORM_PARAMETER_OU_WEBSITE, pOrganizationUnit.getWebsite());
        returnValue = performOrganizationUnitPut(RestClientUtils.encodeUrl(
                ORGANIZATION_UNIT_BY_ID, pOrganizationUnitId), queryParams, formParams);
        return returnValue;
    }

    /**
     * Update study with given id. From the provided study only the following
     * attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>legal note</li>
     * <li>manager.userId</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * @param pStudyId id of the study.
     * @param pStudy study with updated values.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return StudyWrapper
     */
    public StudyWrapper updateStudy(long pStudyId, Study pStudy, String pGroupId) {
        return updateStudy(pStudyId, pStudy, pGroupId, null);
    }

    /**
     * Update study with given id. From the provided study only the following
     * attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>legal note</li>
     * <li>manager.userId</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * @param pStudyId id of the study.
     * @param pStudy study with updated values.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return StudyWrapper
     */
    public StudyWrapper updateStudy(long pStudyId, Study pStudy, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pStudyId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pStudyId must be larger than 0");
        }
        if (pStudy == null) {
            throw new IllegalArgumentException(
                    "Argument pStudy must not be null");
        }
        StudyWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_TOPIC, pStudy.getTopic());
        formParams.add(FORM_PARAMETER_NOTE, pStudy.getNote());
        formParams.add(FORM_PARAMETER_STUDY_LEGAL_NOTE, pStudy.getLegalNote());
        if (pStudy.getManager() != null) {
            formParams.add(FORM_PARAMETER_MANAGER_USER_ID, pStudy.
                    getManager().getUserId().toString());
        }
        if (pStudy.getStartDate() != null) {
            formParams.add(FORM_PARAMETER_START_DATE, Long.toString(pStudy.
                    getStartDate().getTime()));
        }
        if (pStudy.getEndDate() != null) {
            formParams.add(FORM_PARAMETER_END_DATE, Long.toString(pStudy.
                    getEndDate().getTime()));
        }
        returnValue = performStudyPut(RestClientUtils.encodeUrl(STUDY_BY_ID,
                pStudyId), queryParams, formParams);
        return returnValue;
    }

    /**
     * Update investigation with given id. From the provided investigation only
     * the following attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>description</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * @param pInvestigationId id of the investigation.
     * @param pInvestigation investigation with updated values.
     * @param pGroupId id of the group e.g. USERS
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper updateInvestigation(long pInvestigationId,
            Investigation pInvestigation, String pGroupId) {
        return updateInvestigation(pInvestigationId, pInvestigation, pGroupId,
                null);
    }

    /**
     * Update investigation with given id. From the provided investigation only
     * the following attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>description</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * @param pInvestigationId id of the investigation.
     * @param pInvestigation investigation with updated values.
     * @param pGroupId id of the group e.g. USERS
     * @param pSecurityContext security context
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper updateInvestigation(long pInvestigationId,
            Investigation pInvestigation, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pInvestigationId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pInvestigationId must be larger than 0");
        }

        if (pInvestigation == null) {
            throw new IllegalArgumentException(
                    "Argument pInvestigation must not be null");
        }

        InvestigationWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_TOPIC, pInvestigation.getTopic());
        formParams.add(FORM_PARAMETER_NOTE, pInvestigation.getNote());
        formParams.add(Constants.REST_PARAMETER_DESCRIPTION, pInvestigation.
                getDescription());
        if (pInvestigation.getStartDate() != null) {
            formParams.add(FORM_PARAMETER_START_DATE, Long.toString(
                    pInvestigation.getStartDate().getTime()));
        }
        if (pInvestigation.getEndDate() != null) {
            formParams.add(FORM_PARAMETER_END_DATE, Long.toString(
                    pInvestigation.getEndDate().getTime()));
        }
        returnValue = performInvestigationPut(RestClientUtils.encodeUrl(
                INVESTIGATION_BY_ID, pInvestigationId), queryParams, formParams);
        return returnValue;
    }

    /**
     * Update digital object. From the provided digital object only the
     * following attributes are submitted:
     *
     * <ul>
     * <li>label</li>
     * <li>uploader.userId</li>
     * <li>note</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * @param pObjectId id of the digital object.
     * @param pDigitalObject new digital object with updated values.
     * @param pGroupId Name of the group the digital object is associated with
     * (e.g. USERS)
     *
     * @return DigitalObjectWrapper.
     */
    public DigitalObjectWrapper updateDigitalObject(long pObjectId,
            String pGroupId, DigitalObject pDigitalObject) {
        return updateDigitalObject(pObjectId, pDigitalObject, pGroupId, null);
    }

    /**
     * Update digital object. From the provided digital object only the
     * following attributes are submitted:
     *
     * <ul>
     * <li>label</li>
     * <li>uploader.userId</li>
     * <li>note</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * @param pObjectId id of the digital object.
     * @param pDigitalObject new digital object with updated values.
     * @param pGroupId Name of the group the digital object is associated with
     * (e.g. USERS)
     * @param pSecurityContext security context
     * @return DigitalObjectWrapper.
     */
    public DigitalObjectWrapper updateDigitalObject(long pObjectId,
            DigitalObject pDigitalObject, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pObjectId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pObjectId must be larger than 0");
        }
        if (pDigitalObject == null) {
            throw new IllegalArgumentException(
                    "Argument pDigitalObject must not be null");
        }

        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_DIGITAL_OBJECT_LABEL, pDigitalObject.
                getLabel());
        formParams.add(FORM_PARAMETER_NOTE, pDigitalObject.getNote());
        if (pDigitalObject.getStartDate() != null) {
            formParams.add(FORM_PARAMETER_START_DATE, Long.toString(
                    pDigitalObject.getStartDate().getTime()));
        }
        if (pDigitalObject.getEndDate() != null) {
            formParams.add(FORM_PARAMETER_END_DATE, Long.toString(
                    pDigitalObject.getEndDate().getTime()));
        }
        returnValue = performDigitalObjectPut(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_BY_ID, pObjectId), queryParams, formParams);
        return returnValue;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="add[OrganizationUnit|MetadataSchema|Task|ParticipantToInvestigation|RelationToStudy|ExperimenterToDigitalObject|Study|InvestigationToStudy|DigitalObjectToInvestigation|DigitalObjectType|DigitalObjectTypeToDigitalObject]">
    /**
     * Add an organization unit.
     *
     * @param pOrganizationUnit new OrganizationUnit instance to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper addOrganizationUnit(
            OrganizationUnit pOrganizationUnit, String pGroupId) {
        return addOrganizationUnit(pOrganizationUnit, pGroupId, null);
    }

    /**
     * Add a new OrganizationUnit.
     *
     * @param pOrganizationUnit new OrganizationUnit instance to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return OrganizationUnitWrapper
     */
    public OrganizationUnitWrapper addOrganizationUnit(
            OrganizationUnit pOrganizationUnit, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pOrganizationUnit == null) {
            throw new IllegalArgumentException(
                    "Argument pOrganizationUnit must not be null");
        }
        OrganizationUnitWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        if (pOrganizationUnit.getManager() != null) {
            formParams.add(FORM_PARAMETER_MANAGER_USER_ID, pOrganizationUnit.
                    getManager().getUserId().toString());
        }
        formParams.add(FORM_PARAMETER_OU_NAME, pOrganizationUnit.getOuName());
        formParams.add(FORM_PARAMETER_OU_ADDRESS, pOrganizationUnit.getAddress());
        formParams.add(FORM_PARAMETER_OU_CITY, pOrganizationUnit.getCity());
        formParams.add(FORM_PARAMETER_OU_ZIP_CODE, pOrganizationUnit.getZipCode());
        formParams.add(FORM_PARAMETER_OU_COUNTRY, pOrganizationUnit.getCountry());
        formParams.add(FORM_PARAMETER_OU_WEBSITE, pOrganizationUnit.getWebsite());
        returnValue = performOrganizationUnitPost(ORGANIZATION_UNITS, queryParams, formParams);
        return returnValue;
    }

    /**
     * Add a new MetadataSchema.
     *
     * @param pMetadataSchema new MetadataSchema instance to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper addMetadataSchema(
            MetaDataSchema pMetadataSchema, String pGroupId) {
        return addMetadataSchema(pMetadataSchema, pGroupId, null);
    }

    /**
     * Add a new MetadataSchema.
     *
     * @param pMetadataSchema new MetadataSchema instance to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return MetadataSchemaWrapper
     */
    public MetadataSchemaWrapper addMetadataSchema(
            MetaDataSchema pMetadataSchema, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pMetadataSchema == null) {
            throw new IllegalArgumentException(
                    "Argument pMetadataSchema must not be null.");
        }

        MetadataSchemaWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_METADATA_SCHEMA_ID, pMetadataSchema.
                getSchemaIdentifier());
        formParams.add(FORM_PARAMETER_METADATA_SCHEMA_URL, pMetadataSchema.
                getMetaDataSchemaUrl());

        returnValue = performMetadataSchemaPost(METADATA_SCHEMAS, queryParams,
                formParams);
        return returnValue;
    }

    /**
     * Add a Task.
     *
     * @param pTask new Task instance to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return TaskWrapper
     */
    public TaskWrapper addTask(Task pTask, String pGroupId) {
        return addTask(pTask, pGroupId, null);
    }

    /**
     * Add a new Task.
     *
     * @param pTask new Task instance to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return TaskWrapper
     */
    public TaskWrapper addTask(Task pTask, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pTask == null) {
            throw new IllegalArgumentException(
                    "Argument pTask must not be null.");
        }

        TaskWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_TASK_NAME, pTask.getTask());

        returnValue = performTaskPost(TASKS, queryParams, formParams);
        return returnValue;
    }

    /**
     * Add a participant to an investigation.
     *
     * @param pInvestigationId The id of the investigation.
     * @param pParticipant The participant to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper addParticipantToInvestigation(
            long pInvestigationId, Participant pParticipant, String pGroupId) {
        return addParticipantToInvestigation(pInvestigationId, pParticipant,
                pGroupId, null);
    }

    /**
     * Add a participant to an investigation.
     *
     * @param pInvestigationId The id of the investigation.
     * @param pParticipant The pariticipant to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper addParticipantToInvestigation(
            long pInvestigationId, Participant pParticipant, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pInvestigationId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pInvestigationId must be larger than 0");
        }
        if (pParticipant == null || pParticipant.getUser() == null) {
            throw new IllegalArgumentException(
                    "Neither participant nor participant.getUser() must be null.");
        }

        InvestigationWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_USERDATA_ID, Long.toString(pParticipant.
                getUser().getUserId()));
        if (pParticipant.getTask() != null) {
            formParams.add(FORM_PARAMETER_TASK_ID, Long.toString(pParticipant.
                    getTask().getTaskId()));
        }
        returnValue = performInvestigationPost(RestClientUtils.encodeUrl(
                PARTICIPANTS_BY_INVESTIGATION, pInvestigationId), queryParams,
                formParams);
        return returnValue;
    }

    /**
     * Add a metadata schema to an investigation.
     *
     * @param pInvestigationId The id of the investigation.
     * @param pSchema MetadataSchema to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper addMetadataSchemaToInvestigation(
            long pInvestigationId, MetaDataSchema pSchema, String pGroupId) {
        return addMetadataSchemaToInvestigation(pInvestigationId, pSchema,
                pGroupId, null);
    }

    /**
     * Add a metadata schema to an investigation.
     *
     * @param pInvestigationId The id of the investigation.
     * @param pSchema MetadataSchema to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper addMetadataSchemaToInvestigation(
            long pInvestigationId, MetaDataSchema pSchema, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pInvestigationId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pInvestigationId must be larger than 0");
        }

        if (pSchema == null) {
            throw new IllegalArgumentException(
                    "Argument pSchema must not be null.");
        }
        InvestigationWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_METADATA_SCHEMA_ID, Long.toString(pSchema.
                getId()));
        returnValue = performInvestigationPost(RestClientUtils.encodeUrl(
                METADATA_SCHEMA_BY_INVESTIGATION, pInvestigationId), queryParams,
                formParams);
        return returnValue;
    }

    /**
     * Add a relation to a study.
     *
     * @param pStudyId The id of the study.
     * @param pRelation The relation to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return StudyWrapper
     */
    public StudyWrapper addRelationToStudy(long pStudyId, Relation pRelation,
            String pGroupId) {
        return addRelationToStudy(pStudyId, pRelation, pGroupId, null);
    }

    /**
     * Add a relation to a study.
     *
     * @param pStudyId The id of the study.
     * @param pRelation The relation to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return StudyWrapper
     */
    public StudyWrapper addRelationToStudy(long pStudyId, Relation pRelation,
            String pGroupId, SimpleRESTContext pSecurityContext) {
        if (pStudyId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pStudyId must be larger than 0");
        }
        if (pRelation == null || pRelation.getOrganizationUnit() == null) {
            throw new IllegalArgumentException(
                    "Neither pRelation nor pRelation.getOrganizationUnit() must be null.");
        }

        StudyWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_ORGANIZATION_UNIT_ID, Long.toString(
                pRelation.getOrganizationUnit().getOrganizationUnitId()));
        if (pRelation.getTask() != null) {
            formParams.add(FORM_PARAMETER_TASK_ID, Long.toString(pRelation.
                    getTask().getTaskId()));
        }
        returnValue = performStudyPost(RestClientUtils.encodeUrl(
                RELATIONS_BY_STUDY, pStudyId), queryParams, formParams);
        return returnValue;
    }

    /**
     * Add study to group with given id. From the provided study only the
     * following attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>legal note</li>
     * <li>manager.userId</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * Organization units are currently not supported. Also provided
     * investigations are not submitted directly but must be added in another
     * call.
     *
     * @param pStudy new study instance to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return StudyWrapper
     */
    public StudyWrapper addStudy(Study pStudy, String pGroupId) {
        return addStudy(pStudy, pGroupId, null);
    }

    /**
     * Add study to group with given id. From the provided study only the
     * following attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>legal note</li>
     * <li>manager.userId</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * Organization units are currently not supported. Also provided
     * investigations are not submitted directly but must be added in another
     * call.
     *
     * @param pStudy new study instance to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return StudyWrapper
     */
    public StudyWrapper addStudy(Study pStudy, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pStudy == null) {
            throw new IllegalArgumentException(
                    "Argument pStudy must not be null.");
        }
        StudyWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_TOPIC, pStudy.getTopic());
        formParams.add(FORM_PARAMETER_NOTE, pStudy.getNote());
        formParams.add(FORM_PARAMETER_STUDY_LEGAL_NOTE, pStudy.getLegalNote());
        if (pStudy.getManager() != null) {
            formParams.add(FORM_PARAMETER_MANAGER_USER_ID, pStudy.
                    getManager().getUserId().toString());
        }

        if (pStudy.getStartDate() != null) {
            formParams.add(FORM_PARAMETER_START_DATE, Long.toString(pStudy.
                    getStartDate().getTime()));
        }
        if (pStudy.getEndDate() != null) {
            formParams.add(FORM_PARAMETER_END_DATE, Long.toString(pStudy.
                    getEndDate().getTime()));
        }
        returnValue = performStudyPost(STUDIES, queryParams, formParams);
        return returnValue;
    }

    /**
     * Add an investigation to the study with the provided id. From the provided
     * investigation only the following attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>description</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * Participants and metadata schemas are currently not supported. Also
     * provided digital objects are not submitted directly but must be added in
     * another call.
     *
     * @param pStudyId id of the study.
     * @param pInvestigation the investigation to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper addInvestigationToStudy(long pStudyId,
            Investigation pInvestigation, String pGroupId) {
        return addInvestigationToStudy(pStudyId, pInvestigation, pGroupId, null);
    }

    /**
     * Add an investigation to the study with the provided id. From the provided
     * investigation only the following attributes are submitted:
     *
     * <ul>
     * <li>topic</li>
     * <li>note</li>
     * <li>description</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * Participants and metadata schemas are currently not supported. Also
     * provided digital objects are not submitted directly but must be added in
     * another call.
     *
     * @param pStudyId id of the study.
     * @param pInvestigation the investigation to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context.
     *
     * @return InvestigationWrapper
     */
    public InvestigationWrapper addInvestigationToStudy(long pStudyId,
            Investigation pInvestigation, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pStudyId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pStudyId must larger than 0");
        }
        if (pInvestigation == null) {
            throw new IllegalArgumentException(
                    "Argument pInvestigation must not be null.");
        }
        InvestigationWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_TOPIC, pInvestigation.getTopic());
        formParams.add(FORM_PARAMETER_NOTE, pInvestigation.getNote());
        formParams.add(Constants.REST_PARAMETER_DESCRIPTION, pInvestigation.
                getDescription());
        if (pInvestigation.getStartDate() != null) {
            formParams.add(FORM_PARAMETER_START_DATE, Long.toString(
                    pInvestigation.getStartDate().getTime()));
        }
        if (pInvestigation.getEndDate() != null) {
            formParams.add(FORM_PARAMETER_END_DATE, Long.toString(
                    pInvestigation.getEndDate().getTime()));
        }
        returnValue = performInvestigationPost(RestClientUtils.encodeUrl(
                INVESTIGATION_BY_STUDY, pStudyId), queryParams, formParams);
        return returnValue;
    }

    /**
     * Add a digital object to the investigation with the provided id. From the
     * provided digital object only the following attributes are submitted:
     *
     * <ul>
     * <li>label</li>
     * <li>uploader.userId</li>
     * <li>note</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * Experiments are currently not supported. Also a provided digital object
     * id is not submitted but will be generated on the server side.
     *
     * @param pInvestigationId id of the investigation.
     * @param pDigitalObject the digital object to add.
     * @param pGroupId id of the group e.g. USERS
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper addDigitalObjectToInvestigation(
            long pInvestigationId, DigitalObject pDigitalObject, String pGroupId) {
        return addDigitalObjectToInvestigation(pInvestigationId, pDigitalObject,
                pGroupId, null);
    }

    /**
     * Add a digital object to the investigation with the provided id. From the
     * provided digital object only the following attributes are submitted:
     *
     * <ul>
     * <li>label</li>
     * <li>uploader.userId</li>
     * <li>note</li>
     * <li>start date</li>
     * <li>end date</li>
     * </ul>
     *
     * Experiments are currently not supported. Also a provided digital object
     * id is not submitted but will be generated on the server side.
     *
     * @param pInvestigationId id of the investigation.
     * @param pDigitalObject the digital object to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper addDigitalObjectToInvestigation(
            long pInvestigationId, DigitalObject pDigitalObject, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pInvestigationId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pInvestigationId must larger than 0");
        }
        if (pDigitalObject == null) {
            throw new IllegalArgumentException(
                    "Argument pDigitalObject must not be null.");
        }

        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_DIGITAL_OBJECT_LABEL, pDigitalObject.
                getLabel());
        if (pDigitalObject.getUploader() != null) {
            formParams.add(FORM_PARAMETER_DIGITAL_OBJECT_UPLOADER_ID,
                    pDigitalObject.getUploader().getUserId().toString());
        }
        formParams.add(FORM_PARAMETER_NOTE, pDigitalObject.getNote());
        if (pDigitalObject.getStartDate() != null) {
            formParams.add(FORM_PARAMETER_START_DATE, Long.toString(
                    pDigitalObject.getStartDate().getTime()));
        }
        if (pDigitalObject.getEndDate() != null) {
            formParams.add(FORM_PARAMETER_END_DATE, Long.toString(
                    pDigitalObject.getEndDate().getTime()));
        }
        returnValue = performDigitalObjectPost(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_BY_INVESTIGATION, pInvestigationId), queryParams,
                formParams);
        return returnValue;
    }

    /**
     * Add an experimenter to a digital object.
     *
     * @param pObjectId id of the digital object.
     * @param pExperimenter the experimenter to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper addExperimenterToDigitalObject(long pObjectId,
            UserData pExperimenter, String pGroupId) {
        return addExperimenterToDigitalObject(pObjectId, pExperimenter, pGroupId,
                null);
    }

    /**
     * Add an experimenter to the digital object with the provided id.
     *
     * @param pObjectId id of the digital object.
     * @param pExperimenter the experimenter to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return DigitalObjectWrapper
     */
    public DigitalObjectWrapper addExperimenterToDigitalObject(long pObjectId,
            UserData pExperimenter, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pObjectId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pObjectId must larger than 0");
        }
        if (pExperimenter == null) {
            throw new IllegalArgumentException(
                    "Argument pExperimenter must not be null.");
        }
        DigitalObjectWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_USERDATA_ID, pExperimenter.getUserId());
        returnValue = performDigitalObjectPost(RestClientUtils.encodeUrl(
                EXPERIMENTERS_BY_DIGITAL_OBJECT, pObjectId), queryParams, formParams);
        return returnValue;
    }

    /**
     * Add a transition for a digital object. This method call expects
     * pOtherObject to be the output object and 'default' to be the used data
     * organization view.
     *
     * @param pObject The digital object to add the transition for.
     * @param pOtherObject The other digital object that is part of the
     * transition.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper addTransitionToDigitalObject(DigitalObject pObject, DigitalObject pOtherObject, String pGroupId) {
        return addTransitionToDigitalObject(pObject, pOtherObject, Constants.DEFAULT_VIEW, pOtherObject, pGroupId, null);
    }

    /**
     * Add a transition for a digital object.
     *
     * @param pObject The digital object to add the transition for.
     * @param pOtherObject The other digital object that is part of the
     * transition.
     * @param pDataOrganizationViewName The name of the data organization view
     * used from the input object.
     * @param pOutputObject The output object of this transition. Should be
     * either equal to pObject or to pOtherObject.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper addTransitionToDigitalObject(DigitalObject pObject, DigitalObject pOtherObject, String pDataOrganizationViewName,
            DigitalObject pOutputObject, String pGroupId) {
        return addTransitionToDigitalObject(pObject, pOtherObject, pDataOrganizationViewName, pOutputObject, pGroupId, null);
    }

    /**
     * Add a transition for a digital object.
     *
     * @param pObject The digital object to add the transition for.
     * @param pOtherObject The other digital object that is part of the
     * transition.
     * @param pDataOrganizationViewName The name of the data organization view
     * used from the input object.
     * @param pOutputObject The output object of this transition. Should be
     * either equal to pObject or to pOtherObject.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper addTransitionToDigitalObject(DigitalObject pObject, DigitalObject pOtherObject, String pDataOrganizationViewName,
            DigitalObject pOutputObject, String pGroupId, SimpleRESTContext pSecurityContext) {

        return addTransitionToDigitalObject(pObject, pOtherObject, pDataOrganizationViewName, pOutputObject, TransitionType.NONE, null, pGroupId, pSecurityContext);
    }

    /**
     * Add a transition for a digital object. If a transition type is provided
     * together with according typeData the typeData has to fit the server-sided
     * configured type handler for the type. Currentlyu supported types and
     * expected typeData formats are the following:
     *
     * <table summary="Supported transition types">
     * <tr>
     * <td><b>Type</b></td>
     * <td><b>Description</b></td>
     * <td><b>TypeData</b></td>
     * </tr>
     * <tr>
     * <td>NONE</td><td>No type (default)</td><td>none</td>
     * <td>DATAWORKFLOW</td><td>The transition was created by a DataWorkflow
     * task. More details on the task are available via the provided task
     * id.</td><td>Single DataWorkflow task id provided in the form
     * <i>{"transitionEntityId":"4711"}</i>. A task with this id must exists in
     * the DataWorkflowTask table.</td>
     * <td>ELASTICSEARCH</td><td>Type for providing a custom JSON document that
     * can be further processed/registered by a configured handler. The handler
     * is configured on the server side in datamanager.xml</td><td>Custom JSON
     * document depending on the configured handler.</td>
     * </tr>
     * </table>
     *
     * As there is no way to check the server-side configuration of the
     * transition handlers, the use of this method requires knowledge on how the
     * server-side repository system is configured. However, unless there are
     * other information available one should assume that the table above
     * applies. In case of the ELASTICSEARCH type please contact your repository
     * system provide regarding the expected data structure.
     *
     * @param pObject The digital object to add the transition for.
     * @param pOtherObject The other digital object that is part of the
     * transition.
     * @param pDataOrganizationViewName The name of the data organization view
     * used from the input object.
     * @param pOutputObject The output object of this transition. Should be
     * either equal to pObject or to pOtherObject.
     * @param type The DigitalObjectTransition type.
     * @param typeData The JSON data associated with the transition. The format
     * of the data depends on the type. See the table above for more
     * information.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTransitionWrapper
     */
    public DigitalObjectTransitionWrapper addTransitionToDigitalObject(DigitalObject pObject, DigitalObject pOtherObject, String pDataOrganizationViewName,
            DigitalObject pOutputObject, TransitionType type, String typeData, String pGroupId, SimpleRESTContext pSecurityContext) {

        if (pObject == null || pObject.getBaseId() == null) {
            throw new IllegalArgumentException("Argument pObject and pObject.baseId must not be null.");
        }

        if (pOtherObject == null || pOtherObject.getBaseId() == null) {
            throw new IllegalArgumentException("Argument pOtherObject and pOtherObject.baseId must not be null.");
        }

        if (type != null && type != TransitionType.NONE && typeData == null) {
            throw new IllegalArgumentException("Argument typeData must not be null or TransitionType.NONE if argument type is not null.");
        }

        if (pOutputObject == null) {
            pOutputObject = pObject;
        }

        if (pDataOrganizationViewName == null) {
            pDataOrganizationViewName = Constants.DEFAULT_VIEW;
        }

        if (!Objects.equals(pOutputObject.getBaseId(), pOtherObject.getBaseId()) && !Objects.equals(pOutputObject.getBaseId(), pObject.getBaseId())) {
            throw new IllegalArgumentException("BaseId of argument pOutputObject must either match pObject.getBaseId() or pOtherObject.getBaseId()");
        }

        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_OTHER_ID, pOtherObject.getBaseId().toString());
        formParams.add(FORM_PARAMETER_VIEW_NAME, pDataOrganizationViewName);
        formParams.add(FORM_PARAMETER_OUTPUT_ID, pOutputObject.getBaseId().toString());

        if (!TransitionType.NONE.equals(type) && type != null && typeData != null) {
            formParams.add(FORM_PARAMETER_TRANSITION_TYPE, type.toString());
            formParams.add(FORM_PARAMETER_TRANSITION_TYPE_DATA, typeData);
        }
        return performDigitalObjectTransitionPost(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_BY_ID_TRANSITION, pObject.getBaseId()), queryParams, formParams);
    }

    /**
     * Add a new digital object type.
     *
     * @param pObjectType The new digital object type to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper addDigitalObjectType(
            DigitalObjectType pObjectType, String pGroupId) {
        return addDigitalObjectType(pObjectType, pGroupId, null);
    }

    /**
     * Add a new digital object type.
     *
     * @param pObjectType The new digital object type to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper addDigitalObjectType(
            DigitalObjectType pObjectType, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pObjectType == null) {
            throw new IllegalArgumentException(
                    "Argument pObjectType must not be null.");
        }

        if (pObjectType.getTypeDomain() == null || pObjectType.getIdentifier() == null) {
            throw new IllegalArgumentException(
                    "Neither typeDomain nor identifiert of provided object type must be null.");
        }

        DigitalObjectTypeWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_OBJECT_TYPE_DOMAIN, pObjectType.getTypeDomain());
        formParams.add(FORM_PARAMETER_OBJECT_TYPE_IDENTIFIER, pObjectType.getIdentifier());
        formParams.add(FORM_PARAMETER_OBJECT_TYPE_VERSION, Integer.toString(pObjectType.getVersion()));
        if (pObjectType.getDescription() != null) {
            formParams.add(FORM_PARAMETER_OBJECT_TYPE_DESCRIPTION, pObjectType.getDescription());
        }

        return performDigitalObjectTypePost(DIGITAL_OBJECT_TYPES, queryParams,
                formParams);
    }

    /**
     * Add a digital object type to the digital object with the provided id.
     *
     * @param pObjectId The id of the digital object.
     * @param pType The digital object type to add.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper addDigitalObjectTypeToDigitalObject(
            long pObjectId, DigitalObjectType pType, String pGroupId) {
        return addDigitalObjectTypeToDigitalObject(pObjectId, pType,
                pGroupId, null);
    }

    /**
     * Add a digital object type to the digital object with the provided id.
     *
     * @param pObjectId The id of the digital object.
     * @param pType The digital object type to add.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext security context
     *
     * @return DigitalObjectTypeWrapper
     */
    public DigitalObjectTypeWrapper addDigitalObjectTypeToDigitalObject(
            long pObjectId, DigitalObjectType pType, String pGroupId,
            SimpleRESTContext pSecurityContext) {
        if (pObjectId <= 0) {
            throw new IllegalArgumentException(
                    "Argument pObjectId must be larger than 0");
        }
        if (pType == null) {
            throw new IllegalArgumentException(
                    "Argument pType must be null.");
        }

        DigitalObjectTypeWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_OBJECT_TYPE_ID, Long.toString(pType.getId()));
        returnValue = performDigitalObjectTypePost(RestClientUtils.encodeUrl(
                DIGITAL_OBJECT_TYPE_FOR_OBJECT, pObjectId), queryParams,
                formParams);
        return returnValue;
    }

    /**
     * Add a new n:n DigitalObject transition. Input and output objects can be
     * provided as map of object-view entries and list of objects. The client is
     * responsible for proper serialization into JSON. For the objects in the
     * map and the list just the baseIds must be set to a valid value.
     *
     * @param inputObjectViewMap The map of input objects and the according
     * DataOrganization view for the added transition.
     * @param outputObjectList The list of output objects for the added
     * transition.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectTransitionWrapper containing the added transition.
     */
    public DigitalObjectTransitionWrapper addDigitalObjectTransition(Map<DigitalObject, String> inputObjectViewMap, List<DigitalObject> outputObjectList, String pGroupId) {
        return addDigitalObjectTransition(inputObjectViewMap, outputObjectList, TransitionType.NONE, null, pGroupId);
    }

    /**
     * Add a new n:n DigitalObject transition. Input and output objects can be
     * provided as map of object-view entries and list of objects. The client is
     * responsible for proper serialization into JSON. For the objects in the
     * map and the list just the baseIds must be set to a valid value.
     * Furthermore, it is possible to provide TransitionType and type-related
     * data. For more information on providing transitionData please refer to {@link #addTransitionToDigitalObject(edu.kit.dama.mdm.base.DigitalObject, edu.kit.dama.mdm.base.DigitalObject, java.lang.String, edu.kit.dama.mdm.base.DigitalObject, edu.kit.dama.mdm.base.TransitionType, java.lang.String, java.lang.String, edu.kit.dama.rest.SimpleRESTContext)
     * }.
     *
     * @param inputObjectViewMap The map of input objects and the according
     * DataOrganization view for the added transition.
     * @param outputObjectList The list of output objects for the added
     * transition.
     * @param type The transition type.
     * @param typeData Type-related information depending on the provided type.
     * @param pGroupId id of the group e.g. USERS.
     *
     * @return DigitalObjectTransitionWrapper containing the added transition.
     *
     * @see
     * BaseMetaDataRestClient#addTransitionToDigitalObject(edu.kit.dama.mdm.base.DigitalObject,
     * edu.kit.dama.mdm.base.DigitalObject, java.lang.String,
     * edu.kit.dama.mdm.base.DigitalObject,
     * edu.kit.dama.mdm.base.TransitionType, java.lang.String, java.lang.String,
     * edu.kit.dama.rest.SimpleRESTContext)
     */
    public DigitalObjectTransitionWrapper addDigitalObjectTransition(Map<DigitalObject, String> inputObjectViewMap, List<DigitalObject> outputObjectList, TransitionType type, String typeData, String pGroupId) {
        return addDigitalObjectTransition(inputObjectViewMap, outputObjectList, type, typeData, pGroupId, null);
    }

    /**
     * Add a new n:n DigitalObject transition. Input and output objects can be
     * provided as map of object-view entries and list of objects. The client is
     * responsible for proper serialization into JSON. For the objects in the
     * map and the list just the baseIds must be set to a valid value.
     * Furthermore, it is possible to provide TransitionType and type-related
     * data. For more information on providing transitionData please refer to {@link #addTransitionToDigitalObject(edu.kit.dama.mdm.base.DigitalObject, edu.kit.dama.mdm.base.DigitalObject, java.lang.String, edu.kit.dama.mdm.base.DigitalObject, edu.kit.dama.mdm.base.TransitionType, java.lang.String, java.lang.String, edu.kit.dama.rest.SimpleRESTContext)
     * }.
     *
     * @param inputObjectViewMap The map of input objects and the according
     * DataOrganization view for the added transition.
     * @param outputObjectList The list of output objects for the added
     * transition.
     * @param type The transition type.
     * @param typeData Type-related information depending on the provided type.
     * @param pGroupId id of the group e.g. USERS.
     * @param pSecurityContext The security context.
     *
     * @return DigitalObjectTransitionWrapper containing the added transition.
     *
     * @see
     * BaseMetaDataRestClient#addTransitionToDigitalObject(edu.kit.dama.mdm.base.DigitalObject,
     * edu.kit.dama.mdm.base.DigitalObject, java.lang.String,
     * edu.kit.dama.mdm.base.DigitalObject,
     * edu.kit.dama.mdm.base.TransitionType, java.lang.String, java.lang.String,
     * edu.kit.dama.rest.SimpleRESTContext)
     */
    public DigitalObjectTransitionWrapper addDigitalObjectTransition(Map<DigitalObject, String> inputObjectViewMap, List<DigitalObject> outputObjectList, TransitionType type, String typeData, String pGroupId, SimpleRESTContext pSecurityContext) {

        if (inputObjectViewMap == null || inputObjectViewMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument inputObjectViewMap must be null nor empty.");
        }

        if (outputObjectList == null || inputObjectViewMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument outputObjectList must be null nor empty.");
        }

        if (type != null && type != TransitionType.NONE && typeData == null) {
            throw new IllegalArgumentException("Argument typeData must not be null or TransitionType.NONE if argument type is not null.");
        }

        Set<Map.Entry<DigitalObject, String>> entrySet = inputObjectViewMap.entrySet();
        List<JSONObject> jsonMappingsList = new ArrayList<>();
        for (Map.Entry<DigitalObject, String> entry : entrySet) {
            jsonMappingsList.add(new JSONObject("{\"" + entry.getKey().getBaseId() + "\":\"" + entry.getValue() + "\"}"));
        }

        List<Long> jsonOutputObjectList = new ArrayList<>();
        for (DigitalObject entry : outputObjectList) {
            jsonOutputObjectList.add(entry.getBaseId());
        }

        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_INPUT_OBJECT_MAP, new JSONArray(jsonMappingsList).toString());
        formParams.add(FORM_PARAMETER_OUTPUT_OBJECT_LIST, new JSONArray(jsonOutputObjectList).toString());

        if (!TransitionType.NONE.equals(type) && type != null && typeData != null) {
            formParams.add(FORM_PARAMETER_TRANSITION_TYPE, type.toString());
            formParams.add(FORM_PARAMETER_TRANSITION_TYPE_DATA, typeData);
        }
        return performDigitalObjectTransitionPost(DIGITAL_OBJECT_TRANSITIONS, queryParams, formParams);
    }
//</editor-fold>
}
