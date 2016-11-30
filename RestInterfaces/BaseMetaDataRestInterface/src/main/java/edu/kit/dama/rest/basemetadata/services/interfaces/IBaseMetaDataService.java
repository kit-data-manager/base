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
package edu.kit.dama.rest.basemetadata.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType;
import edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation;
import edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema;
import edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit;
import edu.kit.dama.mdm.base.interfaces.IDefaultParticipant;
import edu.kit.dama.mdm.base.interfaces.IDefaultRelation;
import edu.kit.dama.mdm.base.interfaces.IDefaultStudy;
import edu.kit.dama.mdm.base.interfaces.IDefaultTask;
import edu.kit.dama.mdm.base.interfaces.IDefaultUserData;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author mf6319
 */
@Path("/rest/basemetadata")
public interface IBaseMetaDataService extends ICommonRestInterface {

    //<editor-fold defaultstate="collapsed" desc="/transitions/">
    /**
     * Get a DigitalObjectTransition by its id. Available transition for an
     * object can be obtained using
     * {@link #getDigitalObjectDerivationInformation(java.lang.String, java.lang.Long, com.sun.jersey.api.core.HttpContext)}
     * and
     * {@link #getDigitalObjectContributionInformation(java.lang.String, java.lang.Long, com.sun.jersey.api.core.HttpContext)}.
     *
     * @summary Get a DigitalObjectTransition by its id.
     *
     * @param groupId The id of the group the object type belongs to (default:
     * USERS).
     * @param id The id of the transition.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DigitalObjectTransition.
     */
    @GET
    @Path(value = "/transitions/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition>")
    IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectTransitionById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add an n:n transition with single/multiple input object(s) and
     * single/multiple output object(s). In order to be able to describe the
     * transition a transition type might be assigned. Depending on the
     * transition type it might be necessary to provide type data, e.g. a
     * transition entity id that can be used to obtain further information or a
     * JSON document that will be processed by an according processor on the
     * server side.
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
     * @summary Add a DigitalObjectTransition with one or more input objects and
     * one or more output objects.
     *
     * @param groupId The id of the group the object type belongs to (default:
     * USERS).
     * @param inputObjectMap The map of input objects and the according
     * DataOrganization view. This argument must be a single string in the form
     * <i>[{"1":"default"},{"2":"customView"}]</i> where the keys are the
     * digital object baseIds and the values are the view names.
     * @param outputObjectList The list of output digital object baseIds. The
     * argument must be a single string in the form
     * <i>[3,4]</i>.
     * @param type The type enum of this transition. Depending on the type the
     * value of 'typeData' should be provided as documented above.
     * @param typeData The data that can be used to assign additional
     * information to the transition.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The added DigitalObjectTransition.
     */
    @POST
    @Path(value = "/transitions/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition>")
    IEntityWrapper<? extends IDefaultDigitalObjectTransition> addDigitalObjectTransition(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("inputObjectMap") String inputObjectMap,
            @FormParam("outputObjectList") String outputObjectList,
            @FormParam("type") @DefaultValue("NONE") TransitionType type,
            @FormParam("typeData") String typeData,
            @javax.ws.rs.core.Context HttpContext hc);

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="/objectTypes/">
    /**
     * Returns a list of object types. Without any arguments a call to this
     * method returns the first 10 (or less) object type entries accessible by
     * the calling user.
     *
     * @summary Get a list of accessible digital object types.
     *
     * @param groupId The id of the group the object type belongs to (default:
     * USERS).
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of digital object type entities.
     */
    @GET
    @Path(value = "/objectTypes/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType>")
    IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypes(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single object type by its id.
     *
     * @summary Get a single object type by its id.
     *
     * @param groupId The id of the group the object type unit belongs to
     * (default: USERS).
     * @param id The ObjectType id.
     * @param hc The HttpContext for OAuth check.
     *
     * @return One or none single object type entity.
     */
    @GET
    @Path(value = "/objectTypes/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType>")
    IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypeById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a new digital object type.
     *
     * @summary Add a new digital object type.
     *
     * @param groupId The id of the group the object type unit belongs to
     * (default: USERS).
     * @param typeDomain The domain of the digital object type.
     * @param identifier The domain-unique identifier of the digital object
     * type.
     * @param version The digital object type version if there are multiple
     * versions for the same identifier (default: 1).
     * @param description A human readable description of the digital object
     * type.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created object type entity.
     */
    @POST
    @Path(value = "/objectTypes/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType>")
    IEntityWrapper<? extends IDefaultDigitalObjectType> addDigitalObjectType(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("typeDomain") String typeDomain,
            @FormParam("identifier") String identifier,
            @FormParam("version") @DefaultValue("1") int version,
            @FormParam("description") String description,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Return the number of digital object type entries accessible using the
     * provided group id.
     *
     * @summary The number of accessible digital object types.
     *
     * @param groupId The id of the group the object types belong to.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible object types.
     */
    @GET
    @Path(value = "/objectTypes/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType>")
    IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypeCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all digital objects that have the digital object type with the
     * provided id assigned. Currently, neither providing <i>first</i> nor
     * <i>results</i>
     * has any effect. A call to this method always returns all digital objects
     * associated with the provided object type.
     *
     * @summary Get all digital objects that have the digital object type with
     * the provided id assigned.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object type.
     * @param first The first index. This parameter is currently without effect.
     * @param results The max. number of results. This parameter is currently
     * without effect.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of accessible digital objects that have the digital object
     * type with the provided id assigned.
     */
    @GET
    @Path(value = "/objectTypes/{id}/digitalObjects")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectsForDigitalObjectType(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the number of digital objects that have the digital object type with
     * the provided id assigned.
     *
     * @summary Get the number of digital objects that have the digital object
     * type with the provided id assigned.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object type.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible digital objects that have the digital
     * object type with the provided id assigned.
     */
    @GET
    @Path(value = "/objectTypes/{id}/digitalObjects/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectCountForType(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="/organizationUnits/">

    /**
     * Returns a list of accessible organization units. Without any arguments a
     * call to this method returns the first 10 (or less) organization unit
     * entries accessible by the calling user.
     *
     * @summary Get a list of accessible organization units.
     *
     * @param groupId The id of the group the organization unit belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of organization unit entities.
     */
    @GET
    @Path(value = "/organizationUnits/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit>")
    IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnits(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new organization unit via POST request. All relevant attributes
     * can be set via form parameters. If an organization unit was created, the
     * new organization units is returned.
     *
     * @summary Create a new organization unit.
     *
     * @param groupId The group id this organization unit is associated with.
     * @param ouName The name of the organization unit.
     * @param managerUserId The long user id of the manager responsible for the
     * organization unit. If no value is provided, the caller will be set as
     * manager.
     * @param address The address of the organization unit.
     * @param zipCode The zip code of the organization unit.
     * @param city The city of the organization unit.
     * @param country The country of the organization unit.
     * @param website The website of the organization unit.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created organization unit entity.
     */
    @POST
    @Path(value = "/organizationUnits/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit>")
    IEntityWrapper<? extends IDefaultOrganizationUnit> createOrganizationUnit(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("ouName") String ouName,
            @FormParam("managerUserId") @DefaultValue("-1") Long managerUserId,
            @FormParam("address") String address,
            @FormParam("zipCode") String zipCode,
            @FormParam("city") String city,
            @FormParam("country") String country,
            @FormParam("website") String website,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible organization units associated with the provided
     * group.
     *
     * @summary Get the count of all accessible organization units.
     *
     * @param groupId The group id the organization unit is associated with.
     * [default: USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible organization units.
     */
    @GET
    @Path(value = "/organizationUnits/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit>")
    IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnitCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single organization unit by its id.
     *
     * @summary Get an organization unit by its id.
     *
     * @param groupId The group id the organization unit is associated with.
     * @param id The id of the organization unit to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The organization unit with the provided id.
     */
    @GET
    @Path(value = "/organizationUnits/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit>")
    IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnitById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Update the organization unit with the provided id.
     *
     * @summary Update the organization unit with the provided id.
     *
     * @param groupId The group id the organization unit is associated with.
     * @param id The id of the organization unit to update.
     * @param ouName The name of the organization unit.
     * @param address The address of the organization unit.
     * @param zipCode The zip code of the organization unit.
     * @param city The city of the organization unit.
     * @param country The country of the organization unit.
     * @param website The website of the organization unit.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The updated organization unit with the provided id.
     */
    @PUT
    @Path(value = "/organizationUnits/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit>")
    IEntityWrapper<? extends IDefaultOrganizationUnit> updateOrganizationUnit(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("ouName") String ouName,
            @FormParam("address") String address,
            @FormParam("zipCode") String zipCode,
            @FormParam("city") String city,
            @FormParam("country") String country,
            @FormParam("website") String website,
            @javax.ws.rs.core.Context HttpContext hc);
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="/metadataSchemas/">
    /**
     * Returns a list of accessible metadata schemas. Without any arguments a
     * call to this method returns the first 10 (or less) metadata schema
     * entries accessible by the calling user.
     *
     * @summary Get a list of accessible metadata schemas.
     *
     * @param groupId The id of the group the metadata schema belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of metadata schema entities.
     */
    @GET
    @Path(value = "/metadataSchemas/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema>")
    IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemas(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new metadata schema via POST request. All relevant attributes
     * can be set via form parameters. If a metadata schema was created, the new
     * metadata schema is returned. If a metadata schema with the provided
     * identifier already exists, this schema will be returned unmodified.
     *
     * @summary Create a new metadata schema.
     *
     * @param groupId The group id this metadata schema is associated with.
     * @param identifier The unique schema identifier.
     * @param schemaUrl The schema URL.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created/existing metadata schema entity.
     */
    @POST
    @Path(value = "/metadataSchemas/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema>")
    IEntityWrapper<? extends IDefaultMetaDataSchema> createMetadataSchema(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("metadataSchemaId") String identifier,
            @FormParam("schemaUrl") String schemaUrl,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible metadata schemas associated with the provided group.
     *
     * @summary Get the count of all accessible metadata schemas.
     *
     * @param groupId The group id the metadata schema is associated with.
     * [default: USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible metadata schemas.
     */
    @GET
    @Path(value = "/metadataSchemas/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema>")
    IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemaCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single metadata schema by its id.
     *
     * @summary Get a metadata schema by its id.
     *
     * @param groupId The group id the metadata schema is associated with.
     * @param id The id of the metadata schema to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The metadata schema with the provided id.
     */
    @GET
    @Path(value = "/metadataSchemas/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema>")
    IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemaById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="/tasks/">
    /**
     * Returns a list of accessible tasks. Without any arguments a call to this
     * method returns the first 10 (or less) task entries accessible by the
     * calling user.
     *
     * @summary Get a list of accessible tasks.
     *
     * @param groupId The id of the group the tasks belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of task entities.
     */
    @GET
    @Path(value = "/tasks/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultTask>")
    IEntityWrapper<? extends IDefaultTask> getTasks(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new task via POST request. All relevant attributes can be set
     * via form parameters. If a task was created, the new task is returned.
     *
     * @summary Create a new task.
     *
     * @param groupId The group id this task is associated with.
     * @param taskName The task name. It is recommended to use a unique name
     * here.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created task entity.
     *
     */
    @POST
    @Path(value = "/tasks/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultTask>")
    IEntityWrapper<? extends IDefaultTask> createTask(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("taskName") String taskName,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible tasks associated with the provided group.
     *
     * @summary Get the count of all accessible tasks.
     *
     * @param groupId The group id the task is associated with. [default: USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible tasks.
     */
    @GET
    @Path(value = "/tasks/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultTask>")
    IEntityWrapper<? extends IDefaultTask> getTaskCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single task by its id.
     *
     * @summary Get a task by its id.
     *
     * @param groupId The group id the task is associated with.
     * @param id The id of the task to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The task with the provided id.
     */
    @GET
    @Path(value = "/tasks/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultTask>")
    IEntityWrapper<? extends IDefaultTask> getTaskById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="/userData/">
    /**
     * Returns a list of accessible UserData entities. Without any arguments a
     * call to this method returns the first 10 (or less) UserData entities
     * accessible by the calling user.
     *
     * @summary Get a list of accessible UserData entities.
     *
     * @param groupId The id of the group the UserData entity belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of UserData entities.
     */
    @GET
    @Path(value = "/userData/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultUserData>")
    IEntityWrapper<? extends IDefaultUserData> getUserDataEntities(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible UserData entities associated with the provided
     * group.
     *
     * @summary Get the count of all UserData entities in a group.
     *
     * @param groupId The group id the UserData is associated with. [default:
     * USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible UserData entities.
     */
    @GET
    @Path(value = "/userData/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultUserData>")
    IEntityWrapper<? extends IDefaultUserData> getUserDataCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single UserData entity by its id.
     *
     * @summary Get a UserData by its id.
     *
     * @param groupId The group id the UserData is associated with.
     * @param id The id of the UserData to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The UserData with the provided id.
     */
    @GET
    @Path(value = "/userData/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultUserData>")
    IEntityWrapper<? extends IDefaultUserData> getUserDataById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="/participants/">
    /**
     * Get a single participant by its id.
     *
     * @summary Get a participant by its id.
     *
     * @param groupId The group id the participant is associated with.
     * @param id The id of the participant to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The participant with the provided id.
     */
    @GET
    @Path(value = "/participants/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultParticipant>")
    IEntityWrapper<? extends IDefaultParticipant> getParticipantById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="/relations/">
    /**
     * Get a single relation by its id.
     *
     * @summary Get a relation by its id.
     *
     * @param groupId The group id the relation is associated with.
     * @param id The id of the relation to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The relation with the provided id.
     */
    @GET
    @Path(value = "/relations/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultRelation>")
    IEntityWrapper<? extends IDefaultRelation> getRelationById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="/studies/">
    /**
     * Returns a list of accessible studies. Without any arguments a call to
     * this method returns the first 10 (or less) study entries accessible by
     * the calling user in the group USERS.
     *
     * @summary Get a list of accessible studies, which may be filtered by the
     * group they belong to.
     *
     * @param groupId The id of the group the study belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of studies entities.
     */
    @GET
    @Path(value = "/studies/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultStudy>")
    IEntityWrapper<? extends IDefaultStudy> getStudies(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new study via POST request. Almost all relevant attributes can
     * be set via form parameters. Only relations are currently not supported at
     * this point. If a study was created, the new study is returned.
     *
     * @summary Create a new study.
     *
     * @param groupId The group id this study is associated with.
     * @param topic The topic of the study.
     * @param note A note belonging to the study.
     * @param legalNote A legal note belonging to the study.
     * @param managerUserId The long user id of the manager responsible for the
     * study. If no value is provided, the caller will be set as manager.
     * @param startDate The start date of the study. If no value is provided, no
     * start date will be set.
     * @param endDate The end date of the study. If no value is provided, no end
     * date will be set.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created study entity.
     */
    @POST
    @Path(value = "/studies/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultStudy>")
    IEntityWrapper<? extends IDefaultStudy> createStudy(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("topic") String topic,
            @FormParam("note") String note,
            @FormParam("legalNote") String legalNote,
            @FormParam("managerUserId") @DefaultValue("-1") Long managerUserId,
            @FormParam("startDate") @DefaultValue("-1") Long startDate,
            @FormParam("endDate") @DefaultValue("-1") Long endDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a relation to the study with the provided id.
     *
     * @summary Add a relation to the study with the provided id.
     *
     * @param groupId The group id this relation is associated with.
     * @param id The id of the study.
     * @param organizationUnitId The id of the relations organizationUnit.
     * @param taskId The id of the relations task.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The modified study entity.
     */
    @POST
    @Path(value = "/studies/{id}/relations/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultStudy>")
    IEntityWrapper<? extends IDefaultStudy> addRelationToStudy(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("organizationUnitId") Long organizationUnitId,
            @FormParam("taskId") Long taskId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible studies associated with the provided group.
     *
     * @summary Get the count of all accessible groups.
     *
     * @param groupId The group id the study is associated with. [default:
     * USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible studies.
     */
    @GET
    @Path(value = "/studies/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultStudy>")
    IEntityWrapper<? extends IDefaultStudy> getStudyCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single study by its id.
     *
     * @summary Get an study by its id.
     *
     * @param groupId The group id the study is associated with.
     * @param id The id of the study to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The study with the provided id.
     */
    @GET
    @Path(value = "/studies/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultStudy>")
    IEntityWrapper<? extends IDefaultStudy> getStudyById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create an investigation and add it to the study with the provided id.
     *
     * @summary Create and add an investigation to the study with the provided
     * id.
     *
     * @param groupId The group id the study is associated with.
     * @param id The id of the study.
     * @param topic The topic of the investigation.
     * @param note A note belonging to the investigation.
     * @param description A description for the investigation.
     * @param startDate The start date of the investigation. If no value is
     * provided, no start date will be set.
     * @param endDate The end date of the investigation. If no value is
     * provided, no end date will be set.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The added investigation with the provided id.
     */
    @POST
    @Path(value = "/studies/{id}/investigations/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> addInvestigationToStudy(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("topic") String topic,
            @FormParam("note") String note,
            @FormParam("description") String description,
            @FormParam("startDate") @DefaultValue("-1") Long startDate,
            @FormParam("endDate") @DefaultValue("-1") Long endDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Update the study with the provided id. Updates are only allowed for some
     * fields as most attributes are set internally and influence workflows and
     * data handling.
     *
     * @summary Updates the study with the provided id.
     *
     * @param id The id of the study to update.
     * @param groupId The group id the study is associated with.
     * @param topic The topic of the study.
     * @param note Notes linked to the study.
     * @param legalNote A legal note linked to the study.
     * @param startDate The start date of the study.
     * @param endDate The end date of the study.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The updated study serialized.
     */
    @PUT
    @Path(value = "/studies/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultStudy>")
    IEntityWrapper<? extends IDefaultStudy> updateStudyById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("topic") String topic,
            @FormParam("note") String note,
            @FormParam("legalNote") String legalNote,
            @FormParam("startDate") @DefaultValue("-1") Long startDate,
            @FormParam("endDate") @DefaultValue("-1") Long endDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the study with the provided id. Currently, deleting studies is not
     * implemented.
     *
     * @summary Delete the study with the provided id.
     *
     * @param id The id of the study to delete.
     * @param groupId The group id the study is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The HTTP response with an appropriate HTTP response code.
     */
    @DELETE
    @Path(value = "/studies/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response deleteStudyById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="/investigations/">
    /**
     * Returns a list of accessible investigations. Without any arguments a call
     * to this method returns the first 10 (or less) investigation entries
     * accessible by the calling user in the group USERS.
     *
     * @summary Get a list of accessible studies, which may be filtered by the
     * group they belong to.
     *
     * @param groupId The id of the group the investigation belongs to.
     * @param studyId The id of the study the investigation belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of investigation entities.
     */
    @GET
    @Path(value = "/investigations/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> getInvestigations(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("studyId") @DefaultValue(Constants.REST_ALL_INT) Long studyId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible investigations associated with the provided group
     * and study.
     *
     * @summary Get the count of all accessible investigations in the provided
     * group and study.
     *
     * @param groupId The group id the investigation is associated with.
     * [default: USERS]
     * @param studyId The study id the investigation is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible investigations.
     */
    @GET
    @Path(value = "/investigations/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> getInvestigationCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("studyId") @DefaultValue(Constants.REST_ALL_INT) Long studyId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single investigation by its id.
     *
     * @summary Get an investigation by its id.
     *
     * @param id The id of the investigation to query for.
     * @param groupId The group id the investigation is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The investigation with the provided id.
     */
    @GET
    @Path(value = "/investigations/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> getInvestigationById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a digital object to the investigation with the provided id. If the
     * object is already associated with an investigation, the object is removed
     * from the old investigation before it is added to the new one.
     *
     * @summary Add a digital object to the investigation with the provided id.
     *
     * @param id The id of the investigation to update.
     * @param groupId The group id the investigation is associated with.
     * @param label A label for the digital object.
     * @param uploaderId The user id of the uploader. If no uploaded id is
     * provided, it will be assigned during staging.
     * @param note A note belonging to the digital object.
     * @param startDate The start date of the digital object. If no value is
     * provided, no start date will be set.
     * @param endDate The end date of the digital object. If no value is
     * provided, no end date will be set.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The added digital object with the provided id.
     */
    @POST
    @Path(value = "/investigations/{id}/digitalObjects/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> addDigitalObjectToInvestigation(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("label") String label,
            @FormParam("uploaderId") Long uploaderId,
            @FormParam("note") String note,
            @FormParam("startDate") @DefaultValue("-1") Long startDate,
            @FormParam("endDate") @DefaultValue("-1") Long endDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a participant to the investigation with the provided id.
     *
     * @summary Add a participant to the investigation with the provided id.
     *
     * @param groupId The group id this participant is associated with.
     *
     * @param userDataId The id of the participants userData.
     * @param id The id of the investigation to update.
     * @param taskId The id of the participants task.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The modified investigation entity.
     */
    @POST
    @Path(value = "/investigations/{id}/participants/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> addParticipantToInvestigation(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("userDataId") Long userDataId,
            @FormParam("taskId") Long taskId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a metadata schema to the investigation with the provided id.
     *
     * @summary Add a metadata schema to the investigation with the provided id.
     *
     * @param groupId The group id this metadata schema is associated with.
     * @param id The investigation id.
     * @param metadataSchemaId The id of the metadata schema.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The modified investigation entity.
     */
    @POST
    @Path(value = "/investigations/{id}/metadataSchemas/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> addMetadataSchemaToInvestigation(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("metadataSchemaId") Long metadataSchemaId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Update the investigation with the provided id. Updates are only allowed
     * for some fields as most attributes are set internally and influence
     * workflows and data handling.
     *
     * @summary Updates the investigation with the provided id.
     *
     * @param id The id of the investigation to update.
     * @param groupId The group id the investigation is associated with.
     * @param topic The topic of the investigation.
     * @param note Notes linked to the investigation.
     * @param description A description of the investigation.
     * @param startDate The start date of the study.
     * @param endDate The end date of the study.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The updated investigation.
     */
    @PUT
    @Path(value = "/investigations/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation>")
    IEntityWrapper<? extends IDefaultInvestigation> updateInvestigationById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("topic") String topic,
            @FormParam("note") String note,
            @FormParam("description") String description,
            @FormParam("startDate") @DefaultValue("-1") Long startDate,
            @FormParam("endDate") @DefaultValue("-1") Long endDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the investigation with the provided id. Currently, deleting
     * investigations is not implemented.
     *
     * @summary Delete the investigation with the provided id.
     *
     * @param id The id of the investigation to delete.
     * @param groupId The group id the investigation is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The HTTP response with an appropriate HTTP response code.
     */
    @DELETE
    @Path(value = "/investigations/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response deleteInvestigationById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="/digitalObjects/">
    /**
     * Returns a list of accessible digital objects. Without any arguments a
     * call to this method returns the first 10 (or less) objects accessible by
     * the calling user in the group USERS.
     *
     * @summary Get a list of accessible digital objects, which may be filtered
     * by the group and the investigation they belong to.
     *
     * @param groupId The id of the group the investigation belongs to.
     * @param investigationId The id of the investigaqtion the digital object
     * belongs to.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of digital object entities.
     */
    @GET
    @Path(value = "/digitalObjects/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjects(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("investigationId") @DefaultValue(Constants.REST_ALL_INT) Long investigationId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible digital objects associated with the provided group
     * and investigation.
     *
     * @summary Get the count of all accessible digital objects in the provided
     * group and investigation.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param investigationId The investigation id the object is associated
     * with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible digital objects.
     */
    @GET
    @Path(value = "/digitalObjects/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("investigationId") @DefaultValue(Constants.REST_ALL_INT) Long investigationId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single digital object by its id.
     *
     * @summary Get an digital object by its id.
     *
     * @param id The id of the digital object to query for.
     * @param groupId The group id the digital object is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The digital object with the provided id.
     */
    @GET
    @Path(value = "/digitalObjects/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single digital object by its digital object id.
     *
     * @summary Get an digital object by its digital object id.
     *
     * @param digitalObjectId The digital object identifier of the digital
     * object to query for.
     * @param groupId The group id the digital object is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The digital object with the provided digital object id.
     */
    @GET
    @Path(value = "/digitalObjects/doi/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectByDOI(
            @QueryParam("doi") String digitalObjectId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the types assigned to the digital object identified by <i>id</i>. The
     * result may contain no, one or multiple object types, depending on how the
     * repository uses object types.
     *
     * @summary Get the object types assigned to the digital object identified
     * by the provided id.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of object types assigned to the object with the provided
     * id.
     */
    @GET
    @Path(value = "/digitalObjects/{id}/objectTypes")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType>")
    IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypesForDigitalObject(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a new object types to the digital object identified by <i>id</i>.
     *
     * @summary Add a new object type to the digital object identified by the
     * provided id.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object.
     * @param objectTypeId The id of the object type to add.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The assigned object type.
     */
    @POST
    @Path(value = "/digitalObjects/{id}/objectTypes")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType>")
    IEntityWrapper<? extends IDefaultDigitalObjectType> addDigitalObjectTypeToDigitalObject(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("objectTypeId") Long objectTypeId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the transition(s) the digital object with the provided id is derived
     * from. Typically, only one transition should contribute to a digital
     * object, but this may change in future. Transitions returned by this
     * method will have the object with the provided <i>id</i> in their
     * outputObjects list.
     *
     * @summary Get the transition(s) the digital object with the provided id is
     * derived from.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object.
     * @param hc The HttpContext for OAuth check.
     *
     * @return All transitions having the object with the provided id as input.
     */
    @GET
    @Path(value = "/digitalObjects/{id}/derivedFrom/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition>")
    IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectDerivationInformation(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the transition(s) the digital object with the provided id contributes
     * to. Transitions returned by this method will have the object with the
     * provided <i>id</i> in their inputObjects map.
     *
     * @summary Get the transition(s) the digital object with the provided id
     * contributes to.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object.
     * @param hc The HttpContext for OAuth check.
     *
     * @return All transitions having the object with the provided id as output.
     */
    @GET
    @Path(value = "/digitalObjects/{id}/contributesTo/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition>")
    IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectContributionInformation(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a one to one transition for the digital object with the provided id.
     * By default, the object with the baseId <i>id</i> is expected to be the
     * input of the transition, the object with the baseId <i>otherId</i> is
     * intended to be the output. This can be changed by setting <i>outputId</i>
     * to the value of
     * <i>id</i>. Finally, the DataOrganization view name of the input object
     * that was used to obtain the output object is provided.
     *
     * Currently, it is only possible to add transitions with one source and one
     * target object. This may change in later versions.
     *
     * @summary Add a transition for the digital object with the provided id.
     *
     * @param groupId The group id the object is associated with. [default:
     * USERS]
     * @param id The id of the digital object.
     * @param otherId The id of the other digital object.
     * @param viewName The data organization view name used together with the
     * input object. The default value is Constants.DEFAULT_VIEW.
     * @param outputId The id of the output object. The default value -1 means
     * that outputId = otherId. This can be changed by setting outputId = id.
     * @param type The type enum of this transition. Depending on the type the
     * value of 'typeData' should be provided as documented above.
     * @param typeData The data that can be used to assign additional
     * information to the transition.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The added transition.
     */
    @POST
    @Path(value = "/digitalObjects/{id}/transitions/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition>")
    IEntityWrapper<? extends IDefaultDigitalObjectTransition> addTransitionToDigitalObject(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("otherId") Long otherId,
            @FormParam("viewName") @DefaultValue(value = Constants.DEFAULT_VIEW) String viewName,
            @FormParam("outputId") @DefaultValue(value = Constants.REST_ALL_INT) Long outputId,
            @FormParam("type") @DefaultValue("NONE") TransitionType type,
            @FormParam("typeData") String typeData,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add an experimenter to the digital object with the provided id.
     *
     * @summary Add an experimenter to the digital object with the provided id.
     *
     * @param groupId The group id this experimenter is associated with.
     * @param id The id of the digital object.
     * @param userDataId The id of the experimenters userData.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The modified digital object entity.
     */
    @POST
    @Path(value = "/digitalObjects/{id}/experimenters/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> addExperimenterToDigitalObject(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @FormParam("userDataId") Long userDataId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Update the digital object with the provided id. Updates are only allowed
     * for some fields as most attributes are set internally and influence
     * workflows and data handling.
     *
     * @summary Updates the digital object with the provided id.
     *
     * @param id The id of the digital object to update.
     * @param groupId The group id the digital object is associated with.
     * @param label The labelof the digital object.
     * @param note Notes linked to the digital object.
     * @param startDate The start date of the digital object.
     * @param endDate The end date of the digital object.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The updated investigation.
     */
    @PUT
    @Path(value = "/digitalObjects/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject>")
    IEntityWrapper<? extends IDefaultDigitalObject> updateDigitalObjectById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("label") String label,
            @FormParam("note") String note,
            @FormParam("startDate") @DefaultValue("-1") Long startDate,
            @FormParam("endDate") @DefaultValue("-1") Long endDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the digital object with the provided id. Currently, deleting
     * digital objects is not implemented.
     *
     * @summary Delete the digital object with the provided id.
     *
     * @param id The id of the digital object to delete.
     * @param groupId The group id the digital object is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The HTTP reponse with an appropriate HTTP response code.
     */
    @DELETE
    @Path(value = "/digitalObjects/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response deleteDigitalObjectById(
            @PathParam("id") Long id,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);
    //</editor-fold>

}
