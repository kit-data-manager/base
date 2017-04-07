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
package edu.kit.dama.mdm.core.tools;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.util.SecurableEntityHelper;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.util.AuthorizationUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.jpa.EntityManagerHelper;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KIT Data Manager handles queries to metadata entities and authorization
 * information using two different services. This allows to keep authorization
 * information and metadata in two different system, both most appropiate for
 * each use case. However, in basic installations both services are using the
 * same database allowing the combine both service queries into direct database
 * queries. The AbstractSecureQueryHelper offers a generic implementation on how
 * to query for securable entities by combining authorization information and
 * metadata queries. There are implementations for securable resources of KIT
 * Data Manager's base metadata entities Study, Investigation and DigitalObject
 * as part of the MDM-BaseMetaData module.
 *
 * However, due to way the queries have to be performed, a rather poor
 * performance is expected for repository systems with many entries
 * (&gt;&gt;40K).
 *
 * @param <C> Generic type for which the query is performed.
 *
 * @author jejkal
 */
public abstract class AbstractSecureQueryHelper<C> {

    public enum ORDER {
        ASC,
        DESC;
    }

    private static Logger logger = LoggerFactory.getLogger(AbstractSecureQueryHelper.class);

    private Class<C> clazz;
    private String tableName;

    /**
     * Default constructor.
     */
    public AbstractSecureQueryHelper() {
        this(null);
    }

    /**
     * Default constructor.
     *
     * @param pAlternateTableName Alternative table name if the entity table is
     * not named like the entity.
     */
    public AbstractSecureQueryHelper(String pAlternateTableName) {
        initializeInternal();
        if (pAlternateTableName != null) {
            tableName = pAlternateTableName;
        } else {
            tableName = clazz.getSimpleName();
        }
    }

    /**
     * Perform internal initialization.
     */
    final void initializeInternal() {
        setType(TypeResolver.resolveArgument(this.getClass(), AbstractSecureQueryHelper.class));
    }

    /**
     * Set the type class obtained by the TypeResolver.
     *
     * @param pType The type class.
     *
     * @see edu.​kit.dama.mdm.core.​tools.TypeResolver
     */
    final void setType(Class pType) {
        clazz = pType;
    }

    /**
     * Get the number of all readable resources.
     *
     * @param pMetaDataManager The MetaDataManager used to query for the
     * resources.
     * @param pContext The context used to authorize the access. Attention: User
     * context needed. System context is not applicable here!
     *
     * @return The number of readable resources.
     *
     * @throws UnauthorizedAccessAttemptException If the context is not
     * authorized to access the method or any resource.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public final int getReadableResourceCount(IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResourceCount(pMetaDataManager, null, pContext);
    }

    /**
     * Get the number of readable resources fulfilling the provided detailed
     * query. The detailed query is a part of the overall query which allows to
     * filter by single columns.
     *
     * @param pMetaDataManager The MetaDataManager used to query for the
     * resources.
     * @param pDetailedQuery The detailed query, e.g. <i>name LIKE %John D%</i>
     * @param pContext The context used to authorize the access. Attention: User
     * context needed. System context is not applicable here!
     *
     * @return The number of readable resources.
     *
     * @throws UnauthorizedAccessAttemptException If the context is not
     * authorized to access the method or any resource.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public final int getReadableResourceCount(IMetaDataManager pMetaDataManager, String pDetailedQuery, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        StringBuilder query = new StringBuilder();

        String domain = SecurableEntityHelper.getSecurableResourceDomain(clazz);
        String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(clazz);

        query.append("SELECT COUNT(f) FROM FilterHelper f,").
                append(tableName).append(" o WHERE ");

        query.append("f.userId='").append(pContext.getUserId().getStringRepresentation());
        query.append("' AND ");
        //add group information only if not admin context is used (role ADMINISTRATOR or group SYS_ADMIN)
        if (!AuthorizationUtil.isAdminContext(pContext)) {
            query.append("f.groupId='").append(pContext.getGroupId().getStringRepresentation()).append("' AND ");
        }

        query.append("f.domainId='").append(domain).
                append("' AND f.roleAllowed>=").
                append(Role.GUEST.ordinal()).
                append(" AND f.domainUniqueId=o.").append(uniqueField);

        if (pDetailedQuery != null) {
            query.append(" AND ").append(pDetailedQuery);
        }
        logger.debug("Executing query for readable resources: {}", query);
        return ((Number) pMetaDataManager.findSingleResult(query.toString())).intValue();
    }

    /**
     * Get a list of readable resources fulfilling the provided detailed query.
     * The detailed query is a part of the overall query which allows to filter
     * by single columns. The query will include FilterHelper as well as entity
     * table. Therefore, FilterHelper and entity class must be in the same
     * persistence unit. Furthermore, this method allows to provide a sort
     * order. The order field is the field of the entity annotated as Id.
     *
     * If no annotated field can be found, no order is applied and results will
     * be returned in random order, e.g. according to their last modification
     * time.
     *
     * @param pMetaDataManager The MetaDataManager used to query for the
     * resources.
     * @param pFirstResult The index of the first result.
     * @param pMaxResults The max. number of returned results.
     * @param pDetailedQuery The detailed query. The object queried for is
     * adressed by 'o', e.g. <i>o.name LIKE %John D%</i>
     * @param pOrder The ordering of the results. (default: ASC)
     * @param pContext The context used to authorize the access. Attention: User
     * context needed. System context is not applicable here!
     *
     * @return A list of readable resources.
     *
     * @throws UnauthorizedAccessAttemptException If the context is not
     * authorized to access the method or any resource.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public final List<C> getReadableResources(IMetaDataManager pMetaDataManager, String pDetailedQuery, ORDER pOrder, int pFirstResult, int pMaxResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        logger.debug("Querying for readable resources.");
        logger.debug("{} {}", (pDetailedQuery != null) ? "Using detailed query " : "No detailed query provided.", (pDetailedQuery != null) ? pDetailedQuery : "");
        logger.debug("{} {}", (pOrder != null) ? "Using order " : "No order provided.", (pOrder != null) ? pOrder : "");
        ORDER order = (pOrder != null) ? pOrder : ORDER.ASC;

        StringBuilder query = new StringBuilder();
        logger.debug("Changing authorization context to {}.", pContext);
        IAuthorizationContext ctxOld = pMetaDataManager.getAuthorizationContext();
        pMetaDataManager.setAuthorizationContext(pContext);

        logger.debug("Obtaining domain and domainUniqueId field.");
        String domain = SecurableEntityHelper.getSecurableResourceDomain(clazz);
        String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(clazz);
        logger.debug("Domain: {}, DomainUniqueId: {}", domain, uniqueField);
        List<Object> parameters = new ArrayList<>();
        query.append("SELECT o FROM ").
                append(tableName).
                append(" o, FilterHelper f WHERE o.").
                append(uniqueField).
                append("=f.domainUniqueId AND f.domainId='").
                append(domain).
                append("' AND f.roleAllowed>=").
                append(Role.GUEST.ordinal());

        query.append(" AND f.userId=?1");
        logger.debug("Adding parameter userId with value {}", pContext.getUserId());
        parameters.add(pContext.getUserId().getStringRepresentation());
        //add group information only if not admin context is used (role ADMINISTRATOR or group SYS_ADMIN)
        if (!AuthorizationUtil.isAdminContext(pContext)) {
            logger.debug("Adding parameter groupId with value {}", pContext.getGroupId());
            query.append(" AND f.groupId=?2");
            parameters.add(pContext.getGroupId().getStringRepresentation());
        }

        if (pDetailedQuery != null) {
            logger.debug("Adding detailed query.");
            query.append(" AND ").append(pDetailedQuery);
        }

        String idField = EntityManagerHelper.getIdFieldName(clazz);
        if (idField == null) {
            logger.warn("Failed to determine primary key field of entity {}. Returning unordered list.", clazz);
        } else {
            logger.debug("Adding order statement for field {} and order {}.", idField, order);
            query.append(" ORDER BY o.").append(idField).append(" ").append(order.toString());
        }

        logger.debug("Executing query {}.", query.toString());
        List<C> results = pMetaDataManager.findResultList(query.toString(), parameters.toArray(new Object[]{}), clazz);
        logger.debug("Obtained {} results. Resetting authorization context and returning results.", results.size());
        pMetaDataManager.setAuthorizationContext(ctxOld);
        return results;

        /**
         * Get a list of readable resources fulfilling the provided detailed
         * query. The detailed query is a part of the overall query which allows
         * to filter by single columns. The internal workflow covers two
         * queries: In a first query all resource ids of the appropriate
         * resource type are obtained. In a second query this list is used to
         * query for the according resources. If the number of requested results
         * is larger than 1.000 or if a pDetailedQuery is not 'null', the
         * resource ids are slit into blocks of 1.000 elements and are used in
         * subsequent queries as long as the requested number of results is not
         * reached. This is done for memory reasons. The result list will start
         * at index pFirstResult and will contain max. pMaxResults elements.
         *
         * For databases holding a huge number of resources (40K+) or requested
         * results (&gt;100) this methods could have a poor performance. For
         * fast checks of single resources,
         * {@link #getReadableResources(edu.kit.dama.mdm.core.IMetaDataManager, int, int, edu.kit.dama.authorization.entities.IAuthorizationContext)}
         * should be used.
         *
         * @param pMetaDataManager The MetaDataManager used to query for the
         * resources.
         * @param pFirstResult The index of the first result.
         * @param pMaxResults The max. number of returned results.
         * @param pDetailedQuery The detailed query. The object queried for is
         * adressed by 'o', e.g. <i>o.name LIKE %John D%</i>
         * @param pOrder The ordering of the results. (default: asc)
         * @param pContext The context used to authorize the access. Attention:
         * User context needed. System context is not applicable here!
         *
         * @return A list of readable resources.
         *
         * @throws UnauthorizedAccessAttemptException If the context is not
         * authorized to access the method or any resource.
         */
//        //first, get the total amount of resources matching the query
//        int availableAmount = getReadableResourceCount(pMetaDataManager, pDetailedQuery, pContext);
//        System.out.println("VAIL " + availableAmount);
//        //build the query for selecting resources
//        StringBuilder query = new StringBuilder();
//        query.append("SELECT o FROM ").append(tableName).append(" o");
//
//        if (pDetailedQuery != null) {
//            query.append(" WHERE ").append(pDetailedQuery);
//        }
//        int startIndex = pFirstResult;
//        //query for the first block of resources
//        List<C> results = pMetaDataManager.findResultList(query.toString(), clazz, startIndex, pMaxResults);
//        System.out.println("RES1 " + results.size());
//        //we took the first pMaxResults results
//        startIndex += pMaxResults;
//        availableAmount -= results.size();
//
//        int blockSize = pMaxResults;
//        while (results.size() < pMaxResults && availableAmount > 0) {
//            System.out.println("NEXT " + blockSize);
//            //if we arrive here, results.size() is smaller than maxResults and there are still available elements
//            List<C> nextResults = pMetaDataManager.findResultList(query.toString(), clazz, startIndex, blockSize);
//            //add all results...if results is larger than maxResults it doesn't matter, we'll remove additional results later
//            results.addAll(nextResults);
//            //reduce the available results amount by the number of found results
//            availableAmount -= nextResults.size();
//            //increase the start index by 100
//            startIndex += blockSize;
//            if (blockSize < 500) {
//                blockSize += pMaxResults;
//            }
//        }
//
//        return results.subList(0, Math.min(results.size(), pMaxResults));
//        StringBuilder query = new StringBuilder();
//        String domain = SecurableEntityHelper.getSecurableResourceDomain(clazz);
//        String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(clazz);
//        logger.debug("Querying for domain unique ids of readable resources.");
//        query.append("SELECT f.domainUniqueId FROM FilterHelper f WHERE ");
//        query.append("f.userId='").append(pContext.getUserId().getStringRepresentation());
//        query.append("' AND ");
//        //add group information only if not admin context is used (role ADMINISTRATOR or group SYS_ADMIN)
//        if (!AuthorizationUtil.isAdminContext(pContext)) {
//            query.append("f.groupId='").append(pContext.getGroupId().getStringRepresentation()).append("' AND ");
//        }
//        query.append("f.domainId='").append(domain).append("' AND f.roleAllowed>=").append(Role.GUEST.ordinal());
//
//        logger.debug("Removing fetch graph property for FilterHelper query.");
//        Object fetchGraph = pMetaDataManager.getProperties().get(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
//        pMetaDataManager.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
//        logger.debug("Obtaining list of readable unique ids.");
//
//        //At this point ALL possible resource ids are fetched, which kills performance for large repository instances, but is also necessary.
//        //Reasons are: as later on the detailed query may filter out resources that are accessible in principle. 
//        //In that case, additional resource ids would have to be loaded in order to be able to achieve pMaxResults. The second reason is,
//        //that single resources might be invalid, thus 100 resource ids might be retrieved but only 99 entities are returned which might confuse the user.
//        List<String> uniqueIds = pMetaDataManager.findResultList(query.toString(), String.class);
//        if (fetchGraph != null) {
//            logger.debug("Re-adding previously assigned fetch graph property.");
//            pMetaDataManager.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, fetchGraph);
//        }
//
//        if (uniqueIds.isEmpty()) {
//            logger.warn("No domain unique ids found. Returning empty resources list.");
//            return new ArrayList<>();
//        }
//
//        logger.info("{} domain unique id(s) found. Querying for resources.", uniqueIds.size());
//        List<C> results = new ArrayList();
//        //split query to subqueries
//        int elementCount = uniqueIds.size();
//        int startIndex = 0;
//        //Read results in blocks...the max. block size is 1000 elements per query.
//        //If the number of max. results is smaller than 1000 and no detailed query is provided, 
//        //the max. number of results is used as block size as this should already be the exact number
//        //of required results. If a detailed query is provided, there is a probability of filtered entries.
//        //Therefor, the max. block size will be used to reduce the number of single database queries.
//        //However, due to the check for the current number of results this will be the only query 
//        //if the first query returns 1000 results, pFirstResult is 0 and pMaxResults is LEQ 1000.
//        int stepSize = (pMaxResults < 1000 && pDetailedQuery == null) ? pMaxResults : 1000;
//
//        while (elementCount > 0) {
//            List<String> subList = uniqueIds.subList(startIndex, Math.min(uniqueIds.size(), startIndex + stepSize));
//            query = new StringBuilder();
//            query.append("SELECT o FROM ").append(tableName).append(" o WHERE ");
//            query.append("o.").append(uniqueField).append(" IN ?1");
//
//            if (pDetailedQuery != null) {
//                query.append(" AND ").append(pDetailedQuery);
//            }
//
//            // pMetaDataManager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
//            List<C> subResults = pMetaDataManager.findResultList(query.toString(), new Object[]{subList}, clazz);
//
//            results.addAll(subResults);
//            if (results.size() >= pFirstResult + pMaxResults) {
//                break;
//            }
//            elementCount -= subList.size();
//            startIndex += subList.size();
//        }
//
//        return results.subList(pFirstResult, Math.min(results.size(), pFirstResult + pMaxResults));
    }

    /**
     * Get a single readable resources with a known unique id fulfilling also
     * the provided detailed query. The detailed query is a part of the overall
     * query which allows to filter by single columns. This method is intended
     * to be used for a fast query to check accessability for a known resource.
     *
     * @param pMetaDataManager The MetaDataManager used to query for the
     * resources.
     * @param pUniqueId The unique id of the resource.
     * @param pDetailedQuery The detailed query, e.g. <i>name LIKE %John D%</i>
     * @param pContext The context used to authorize the access. Attention: User
     * context needed. System context is not applicable here!
     *
     * @return A list of readable resources.
     *
     * @throws UnauthorizedAccessAttemptException If the context is not
     * authorized to access the method or any resource.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public final C getReadableResource(IMetaDataManager pMetaDataManager, String pDetailedQuery, String pUniqueId, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(clazz);

        List<C> resources = getReadableResources(pMetaDataManager, ((pDetailedQuery == null) ? null : pDetailedQuery + " AND o." + uniqueField + "='" + pUniqueId + "'"), ORDER.ASC, 0, 1, pContext);
        return (resources.isEmpty()) ? null : resources.get(0);

        /*StringBuilder query = new StringBuilder();
        String domain = SecurableEntityHelper.getSecurableResourceDomain(clazz);
        String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(clazz);
        query.append("SELECT f.domainUniqueId FROM FilterHelper f WHERE ");
        query.append("f.userId='").append(pContext.getUserId().getStringRepresentation());
        query.append("' AND ");
        //add group information only if not admin context is used (role ADMINISTRATOR or group SYS_ADMIN)
        if (!AuthorizationUtil.isAdminContext(pContext)) {
            query.append("f.groupId='").append(pContext.getGroupId().getStringRepresentation()).append("' AND ");
        }
        query.append("f.domainId='").append(domain).append("' AND ")
                .append("f.domainUniqueId='").append(pUniqueId).
                append("' AND f.roleAllowed>=").append(Role.GUEST.ordinal());

        Object fetchGraph = pMetaDataManager.getProperties().get(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
        pMetaDataManager.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
        logger.debug("Obtaining list of readable unique ids.");
        List<String> uniqueIds = pMetaDataManager.findResultList(query.toString(), String.class);
        if (fetchGraph != null) {
            logger.debug("Re-adding previously assigned fetch graph property.");
            pMetaDataManager.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, fetchGraph);
        }

        String uniqueId;
        if (uniqueIds.isEmpty()) {
            logger.warn("No domain unique ids found. Returning null.");
            return null;
        }

        if (uniqueIds.size() > 1) {
            logger.warn("Multiple domain unique ids found. Using first element {}.", uniqueIds.get(0));
        }

        uniqueId = uniqueIds.get(0);
        query = new StringBuilder();
        query.append("SELECT o FROM ").append(tableName).append(" o WHERE ");
        query.append("o.").append(uniqueField).append("=?1");

        if (pDetailedQuery != null) {
            query.append(" AND ").append(pDetailedQuery);
        }

        return pMetaDataManager.findSingleResult(query.toString(), new Object[]{uniqueId}, clazz);*/
 /*StringBuilder query = new StringBuilder();

     String domain = SecurableEntityHelper.getSecurableResourceDomain(clazz);
     String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(clazz);

     query.append("SELECT o FROM FilterHelper f, ").
     append(tableName).append(" o WHERE ");

     query.append("f.userId='").append(pContext.getUserId().getStringRepresentation());
     query.append("' AND ");

     query.append("f.groupId='").append(pContext.getGroupId().getStringRepresentation()).append("' AND ")
     .append("f.domainId='").append(domain).
     append("' AND f.roleAllowed>=").append(Role.GUEST.ordinal()).
     append(" AND f.domainUniqueId=o.").append(uniqueField);

     if (pDetailedQuery != null) {
     query.append(" AND ").append(pDetailedQuery);
     }

     logger.debug("Executing query for accessible resources: {}", query);
     return pMetaDataManager.findResultList(query.toString(), clazz, pFirstResult, pMaxResults);*/
    }

    /**
     * Get a list of all readable resources. The list will start at index
     * pFirstResult and will contain max. pMaxResults elements.
     *
     * @param pMetaDataManager The MetaDataManager used to query for the
     * resources.
     * @param pFirstResult The index of the first result.
     * @param pMaxResults The max. number of returned results.
     * @param pContext The context used to authorize the access. Attention: User
     * context needed. System context is not applicable here!
     *
     * @return A list of readable resources.
     *
     * @throws UnauthorizedAccessAttemptException If the context is not
     * authorized to access the method or any resource.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public final List<C> getReadableResources(IMetaDataManager pMetaDataManager, int pFirstResult, int pMaxResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResources(pMetaDataManager, null, ORDER.ASC, pFirstResult, pMaxResults, pContext);
    }
}
