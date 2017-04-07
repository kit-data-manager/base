/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin.wizard;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.content.mets.BasicMetsExtractor;
import edu.kit.dama.mdm.content.scheduler.jobs.MetadataIndexerJob;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.AuthenticatorFactory;
import edu.kit.dama.rest.util.auth.impl.HTTPAuthenticator;
import edu.kit.dama.rest.util.auth.impl.OAuthAuthenticator;
import edu.kit.dama.scheduler.SchedulerManagement;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.manager.ISchedulerManager;
import edu.kit.dama.staging.ap.impl.BasicStagingAccessPoint;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.ui.admin.login.MainLoginAuthenticator;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class WizardPersistHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(WizardPersistHelper.class);

    private final static String ADMIN_USER_ID = "admin";

    public static final String ADMIN_FIRST_NAME = "adminFirstName";
    public static final String ADMIN_LAST_NAME = "adminLastName";
    public static final String ADMIN_EMAIL = "adminEmail";
    public static final String ADMIN_PASSWORD = "adminPassword";
    public static final String ADMIN_OAUTH_KEY = "adminOAuthKey";
    public static final String ADMIN_OAUTH_SECRET = "adminOAuthSecret";

    public static final String CREATE_ACCESS_POINT = "createAccessPoint";
    public static final String ACCESS_POINT_NAME = "accessPointName";
    public static final String ACCESS_POINT_BASE_URL = "accessPointBaseUrl";
    public static final String ACCESS_POINT_BASE_PATH = "accessPointBasePath";
    public static final String ACCESS_POINT_ADMIN_PASSWORD = "accessPointAdminPassword";

    public static final String METADATA_CREATE_EXTRACTOR = "createMetadataExtractor";
    public static final String METADATA_EXTRACTOR_PROPERTIES = "extractorProperties";

    private StringBuilder messages = null;

    public WizardPersistHelper() {

    }

    public String getMessages() {
        if (messages != null && messages.length() > 0) {
            return messages.toString();
        }
        return "No messages.";
    }

    public boolean persist(Map<String, String> properties) {
        boolean success = false;
        messages = new StringBuilder();
        addMessage("Creating System Users and Groups");
        if (persistSystemUsers(properties)) {
            addMessage("Creating Access Point");
            if (persistAccessPoint(properties)) {
                addMessage("Creating Metadata Extractor");
                if (persistMetadataExtractor(properties)) {
                    messages.append("Successfully stored all data in the database.");
                    success = true;
                }
            }
        }
        return success;
    }

    private boolean persistSystemUsers(Map<String, String> properties) {
        boolean result = true;
        //create admin user
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            //create admin, world and oai-pmh users
            addMessage("Adding user '" + ADMIN_USER_ID + "'.", 1);
            UserServiceLocal.getSingleton().register(new UserId(ADMIN_USER_ID), Role.ADMINISTRATOR, AuthorizationContext.factorySystemContext());
            addMessage("Adding user '" + Constants.WORLD_USER_ID + "'.", 1);
            UserServiceLocal.getSingleton().register(new UserId(Constants.WORLD_USER_ID), Role.GUEST, AuthorizationContext.factorySystemContext());
            addMessage("Adding user '" + Constants.OAI_PMH_USER_ID + "'.", 1);
            UserServiceLocal.getSingleton().register(new UserId(Constants.OAI_PMH_USER_ID), Role.GUEST, AuthorizationContext.factorySystemContext());
            addMessage("System users successfully created.", 1);
            //create users group
            addMessage("Adding user group '" + Constants.USERS_GROUP_ID + "'.", 1);
            GroupServiceLocal.getSingleton().create(new GroupId(Constants.USERS_GROUP_ID), new UserId(ADMIN_USER_ID), AuthorizationContext.factorySystemContext());
            GroupServiceLocal.getSingleton().changeRole(new GroupId(Constants.USERS_GROUP_ID), new UserId(ADMIN_USER_ID), Role.ADMINISTRATOR, AuthorizationContext.factorySystemContext());
            UserGroup usersGroup = new UserGroup();
            usersGroup.setGroupId(Constants.USERS_GROUP_ID);
            usersGroup.setGroupName("Registered Users");
            usersGroup.setDescription("System group containing all registered users.");
            mdm.save(usersGroup);

            //create world group
            addMessage("Adding user group '" + Constants.WORLD_GROUP_ID + "'.", 1);
            GroupServiceLocal.getSingleton().create(new GroupId(Constants.WORLD_GROUP_ID), new UserId(Constants.WORLD_USER_ID), AuthorizationContext.factorySystemContext());

            //add world user to users group
            addMessage("Adding user " + Constants.WORLD_USER_ID + " to group '" + Constants.USERS_GROUP_ID + "'.", 1);
            GroupServiceLocal.getSingleton().addUser(new GroupId(Constants.USERS_GROUP_ID), new UserId(Constants.WORLD_USER_ID), Role.GUEST, AuthorizationContext.factorySystemContext());
            //add OAI-PMH user to world group
            addMessage("Adding user " + Constants.OAI_PMH_USER_ID + " to group '" + Constants.WORLD_GROUP_ID + "'.", 1);
            GroupServiceLocal.getSingleton().addUser(new GroupId(Constants.WORLD_GROUP_ID), new UserId(Constants.OAI_PMH_USER_ID), Role.GUEST, AuthorizationContext.factorySystemContext());
            addMessage("System groups successfully created.", 1);
            //create user data for admin user
            addMessage("Registering user information for administrator.", 1);
            UserData adminUser = new UserData();
            adminUser.setDistinguishedName(ADMIN_USER_ID);
            adminUser.setFirstName(properties.get(ADMIN_FIRST_NAME));
            addMessage("Setting first name to " + adminUser.getFirstName() + ".", 2);
            adminUser.setLastName(properties.get(ADMIN_LAST_NAME));
            addMessage("Setting last name to " + adminUser.getLastName() + ".", 2);
            adminUser.setEmail(properties.get(ADMIN_EMAIL));
            addMessage("Setting email to " + adminUser.getEmail() + ".", 2);
            adminUser.setValidFrom(new Date());
            addMessage("Writing user information to database.", 2);
            mdm.save(adminUser);
            addMessage("Administrator successfully created.", 1);
            //store credentials
            addMessage("Creating web login for administrator.", 1);
            Map<String, String> credentials = new HashMap<>();
            credentials.put("eMail", properties.get(ADMIN_EMAIL));
            addMessage("Setting web login email to '" + properties.get(ADMIN_EMAIL) + "'", 2);
            credentials.put("Password", properties.get(ADMIN_PASSWORD));
            addMessage("Setting web login password to '" + properties.get(ADMIN_PASSWORD) + "'", 2);
            ServiceAccessToken loginToken = new MainLoginAuthenticator().generateServiceAccessToken(new UserId(ADMIN_USER_ID), credentials);
            mdm.save(loginToken);
            addMessage("Web login successfully created.", 1);

            if (properties.get(ADMIN_OAUTH_KEY) != null && properties.get(ADMIN_OAUTH_SECRET) != null) {
                addMessage("Creating OAuth credentials for administrator.", 1);
                AbstractAuthenticator auth = AuthenticatorFactory.getInstance().getAuthenticator("restServiceAccess");
                if (auth != null) {
                    //default rest auth not configured.
                    credentials.clear();
                    credentials.put(OAuthAuthenticator.USER_TOKEN_KEY, properties.get(ADMIN_OAUTH_KEY));
                    addMessage("Setting OAuth key to '" + properties.get(ADMIN_OAUTH_KEY) + "'.", 2);
                    credentials.put(OAuthAuthenticator.USER_SECRET_KEY, properties.get(ADMIN_OAUTH_SECRET));
                    addMessage("Setting OAuth secret to '" + properties.get(ADMIN_OAUTH_SECRET) + "'.", 2);
                    ServiceAccessToken oAuthToken = auth.generateServiceAccessToken(new UserId(ADMIN_USER_ID), credentials);
                    addMessage("Writing OAuth credentials to database.", 2);
                    mdm.save(oAuthToken);
                    addMessage("OAuth credentials successfully created.", 1);
                } else {
                    //not possible
                    addMessage("Failed to create OAuth credentials for administrator. No authenticator 'restServiceAccess' found.");
                    result = false;
                }
            } else {
                addMessage("Skipping creation of OAuth credentials for administrator.", 1);
            }
            //Done!
        } catch (EntityAlreadyExistsException ex) {
            //admin or any group already exists!?
            addMessage("Failed to create system users and groups. Either the administrator or one of the groups USERS or WORLD already exists.");
            LOGGER.error("Failed to create system users and groups. Either the administrator or one of the groups USERS or WORLD already exists.", ex);
            result = false;
        } catch (EntityNotFoundException | UnauthorizedAccessAttemptException ex) {
            //should not happen
            addMessage("Failed to create system users and groups. An internal error occured.");
            LOGGER.error("Failed to create system users and groups. An internal error occured.", ex);
            result = false;
        } catch (SecretEncryptionException ex) {
            //failed to create access tokens
            addMessage("Failed to create web login or OAuth credentials for administrator. An internal error occured.");
            LOGGER.error("Failed to create web login or OAuth credentials for administrator. An internal error occured.", ex);
            result = false;
        } //should never happen
        finally {
            mdm.close();
        }
        return result;
    }

    private boolean persistAccessPoint(Map<String, String> properties) {
        boolean result = true;

        if (properties.get(CREATE_ACCESS_POINT) == null) {
            addMessage("Skip registering webdav access point.", 1);
            return true;
        }

        addMessage("Registering webdav access point.", 1);
        StagingAccessPointConfiguration config = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
        config.setDefaultAccessPoint(true);
        config.setDisabled(false);
        config.setTransientAccessPoint(false);

        config.setName(properties.get(ACCESS_POINT_NAME));
        addMessage("Setting access point name to '" + config.getName() + "'.", 2);
        config.setRemoteBaseUrl(properties.get(ACCESS_POINT_BASE_URL));
        addMessage("Setting access point base URL to '" + config.getRemoteBaseUrl() + "'.", 2);
        config.setLocalBasePath(properties.get(ACCESS_POINT_BASE_PATH));
        addMessage("Setting access point base path to '" + config.getLocalBasePath() + "'.", 2);
        config.setImplementationClass(new BasicStagingAccessPoint().getClass().getCanonicalName());
        addMessage("Setting access point implementation class '" + config.getImplementationClass() + "'.", 2);
        config.setDescription("Default WebDav Access Point created during first start.");
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            addMessage("Writing access point to database.", 2);
            StagingConfigurationPersistence.getSingleton().saveAccessPointConfiguration(config);
            addMessage("Webdav access point successfully created.", 1);

            if (properties.get(ACCESS_POINT_ADMIN_PASSWORD) != null) {
                addMessage("Creating webdav credentials for administrator.", 1);

                AbstractAuthenticator auth = AuthenticatorFactory.getInstance().getAuthenticator("webdav");
                if (auth != null) {
                    Map<String, String> credentials = new HashMap<>();
                    credentials.put(HTTPAuthenticator.USER_NAME_KEY, properties.get(ADMIN_EMAIL));
                    addMessage("Setting webdav username to '" + properties.get(ADMIN_EMAIL) + "'", 2);
                    credentials.put(HTTPAuthenticator.USER_PASSWORD_KEY, properties.get(ACCESS_POINT_ADMIN_PASSWORD));
                    addMessage("Setting webdav password to '" + properties.get(ACCESS_POINT_ADMIN_PASSWORD) + "'", 2);
                    ServiceAccessToken webdavCredentials = auth.generateServiceAccessToken(new UserId(ADMIN_USER_ID), credentials);
                    addMessage("Writing webdav credentials to database.", 2);
                    mdm.save(webdavCredentials);
                    addMessage("Webdav credentials successfully created.", 1);
                } else {
                    //not possible
                    addMessage("Failed to create webdav credentials for administrator. No authenticator 'webdav' found.");
                    result = false;
                }
            } else {
                addMessage("Skipping creation of webdav credentials for administrator.", 1);
            }
        } catch (SecretEncryptionException | UnauthorizedAccessAttemptException ex) {
            //should not happen
            addMessage("Failed to create access point. An internal error occured.");
            LOGGER.error("Failed to create access point. An internal error occured.", ex);
            result = false;
        } finally {
            mdm.close();
        }
        return result;
    }

    private boolean persistMetadataExtractor(Map<String, String> properties) {
        boolean result = true;
        if (properties.get(METADATA_CREATE_EXTRACTOR) == null) {
            return result;
        }
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());

        try {
            MetaDataSchema oai_dc = new MetaDataSchema("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
            MetaDataSchema bmd = new MetaDataSchema("bmd", "http://datamanager.kit.edu/dama/basemetadata/", "http://datamanager.kit.edu/dama/basemetadata/2015-08/basemetadata.xsd");
            MetaDataSchema mets = new MetaDataSchema("bmd", "http://www.loc.gov/METS/", "http://www.loc.gov/standards/mets/mets.xsd");

            addMessage("Writing default metadata schemas to database.", 1);
            addMessage("Writing schema 'oai_dc'.", 2);
            mdm.save(oai_dc);
            addMessage("Writing schema 'bmd'.", 2);
            mdm.save(bmd);
            addMessage("Writing schema 'mets'.", 2);
            mdm.save(mets);
            addMessage("Default metadata schemas successfully written to database.", 1);
            addMessage("Registering staging processor for metadata extraction.", 1);
            StagingProcessor proc = StagingProcessor.factoryNewStagingProcessor();
            proc.setImplementationClass(new BasicMetsExtractor("").getClass().getCanonicalName());
            addMessage("Setting processor implementation class to '" + proc.getImplementationClass() + "'", 2);
            proc.setDefaultOn(true);
            proc.setDisabled(false);
            proc.setDownloadProcessingSupported(false);
            proc.setIngestProcessingSupported(true);
            proc.setPriority((byte) 0);
            addMessage("Setting processor properties", 2);
            proc.setProperties(properties.get(METADATA_EXTRACTOR_PROPERTIES));
            proc.setDescription("Basic METS metadata extractor created automatically at first start.");
            proc.setName("Basic METS Metadata Extractor");
            addMessage("Setting processor name to '" + proc.getName() + "'", 2);

            addMessage("Writing processor to database.", 2);
            proc = StagingConfigurationPersistence.getSingleton().saveStagingProcessor(proc);
            addMessage("Processor with id '" + proc.getId() + "' successfully written to database.", 1);

            addMessage("Registering metadata indexing job.", 1);
            SimpleSchedule collectSchedule = new QuartzSchedule();
            collectSchedule.setName("METS Metadata Indexer");
            addMessage("Setting job name to '" + collectSchedule.getName() + "'", 2);
            collectSchedule.setDescription("Metadata indexing job created automatically at first start.");
            collectSchedule.setScheduleGroup("Metadata");
            addMessage("Setting job group to '" + collectSchedule.getScheduleGroup() + "'", 2);
            MetadataIndexerJob job = new MetadataIndexerJob();
            collectSchedule.setJobClass(job.getClass().getCanonicalName());
            addMessage("Setting job class to '" + collectSchedule.getJobClass() + "'", 2);

            //we are using the default parameters as they are configured
            collectSchedule.setJobParameters(PropertiesUtil.propertiesToString(new Properties()));
            ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
            addMessage("Writing job to database.", 2);
            collectSchedule = manager.addSchedule(collectSchedule);
            addMessage("Job with id '" + collectSchedule.getId() + "' successfully written to database.", 1);

            addMessage("Registering trigger for metadata indexing job.", 1);
            QuartzIntervalTrigger trigger = new QuartzIntervalTrigger();
            trigger.setName("METS Metadata Indexer Trigger");
            addMessage("Setting trigger name to '" + trigger.getName() + "'", 2);
            trigger.setTriggerGroup("Metadata");
            addMessage("Setting trigger group to '" + trigger.getTriggerGroup() + "'", 2);
            trigger.setDescription("Metadata indexing trigger created automatically at first start.");
            trigger.setPriority(0);
            //start immediately
            trigger.setStartDate(new Date());
            //run every 10 seconds
            addMessage("Setting trigger period 5 seconds", 2);
            trigger.setPeriod(5000l);
            //run forever
            trigger.setEndDate(null);
            trigger.setTimes(-1);

            addMessage("Writing job trigger to database.", 2);
            manager.addTrigger(collectSchedule.getId(), trigger);
            addMessage("Job trigger with id '" + trigger.getId() + "' successfully written to database.", 1);
        } catch (IOException | UnauthorizedAccessAttemptException ex) {
            //should not happen
            addMessage("Failed to create metadata extractor. An internal error occured.");
            LOGGER.error("Failed to create metadata extractor. An internal error occured.", ex);
            result = false;
        } finally {
            mdm.close();
        }
        return result;
    }

    private void addMessage(String message) {
        addMessage(message, 0);
    }

    private void addMessage(String message, int level) {
        //add 'level' tabs at the beginning
        for (int i = 0; i < level; i++) {
            messages.append("   ");
        }
        messages.append(message).append("\n");
    }
}
