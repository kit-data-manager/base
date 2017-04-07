/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin;

import com.vaadin.data.Item;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.content.mets.util.MetsBuilder;
import edu.kit.dama.mdm.content.util.DublinCoreHelper;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.rest.dataorganization.services.impl.util.PublicDownloadHandler;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import edu.kit.lsdf.adalapi.AbstractFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class LandingPageComponent extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LandingPageComponent.class);

    private GridLayout mainLayout;

    public LandingPageComponent() {
    }

    public final void update(DigitalObject object, boolean privileged) {
        if (object == null) {
            UIUtils7.GridLayoutBuilder builder = new UIUtils7.GridLayoutBuilder(1, 1);
            builder.fill(new Label("Access to digital object not permitted."), 0, 0);
            mainLayout = builder.getLayout();
            mainLayout.setMargin(true);
            mainLayout.setSpacing(true);
            mainLayout.setStyleName("landing");
            HorizontalLayout hLayout = new HorizontalLayout(mainLayout);
            hLayout.setSizeFull();
            hLayout.setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
            setCompositionRoot(hLayout);
        } else {
            final TextField oidField = new TextField();
            Button searchButton = new Button("Search");
            Button metsButton = new Button("METS");
            final Button dcButton = new Button("DublinCore");
            Button dataButton = new Button("Download");
            VerticalLayout metadataDownloadButtons = new VerticalLayout(dcButton, metsButton);
            UIUtils7.GridLayoutBuilder builder = new UIUtils7.GridLayoutBuilder(3, 6);

            StreamResource metsResource = new StreamResource(() -> {
                try {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    MetsBuilder.init(object).createMinimalMetsDocument(UserData.WORLD_USER).write(bout);
                    return new ByteArrayInputStream(bout.toByteArray());
                } catch (Exception ex) {
                    LOGGER.error("Failed to provide METS document.", ex);
                    UIComponentTools.showError("Failed to initialize METS document for download. Cause: " + ex.getMessage());
                    return null;
                }
            }, object.getDigitalObjectIdentifier() + ".mets.xml");

            StreamResource dcResource = new StreamResource(() -> {
                try {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    DublinCoreHelper.writeDublinCoreDocument(object, UserData.WORLD_USER, bout);
                    return new ByteArrayInputStream(bout.toByteArray());
                } catch (ParserConfigurationException ex) {
                    LOGGER.error("Failed to provide DC document.", ex);
                    UIComponentTools.showError("Failed to initialize DC document for download. Cause: " + ex.getMessage());
                    return null;
                }
            }, object.getDigitalObjectIdentifier() + ".dc.xml");

            StreamResource dataResource = new StreamResource(() -> {
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
                try {
                    IAuthorizationContext ctx = new AuthorizationContext(new UserId(Constants.WORLD_USER_ID), new GroupId(Constants.WORLD_USER_ID), Role.GUEST);
                    if (accessGranted(object, ctx) || accessGranted(object, UIHelper.getSessionContext())) {
                        Response response = new PublicDownloadHandler().prepareStream(object);
                        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                            return (InputStream) response.getEntity();
                        } else {
                            LOGGER.error("Preparation of the public download for object " + object.getDigitalObjectIdentifier() + " returned status " + response.getStatus() + ". Aborting.");
                        }
                    }
                    throw new IOException("Public access not available.");
                } catch (IOException ex) {
                    LOGGER.error("Failed to provide data stream for object " + object.getDigitalObjectIdentifier() + ".", ex);
                    UIComponentTools.showError("Failed to initialize data stream for public download. Probably, the digital object is not publicly available.");
                    return null;
                }
            }, object.getDigitalObjectIdentifier() + ".zip");

            Map<String, String> dcElementMap = new HashMap<>();
            try {
                dcElementMap = DublinCoreHelper.createDublinCoreElementMap(object, UserData.WORLD_USER);
            } catch (ParserConfigurationException ex) {
                LOGGER.error("Failed to create DC metadata for object with id " + object.getDigitalObjectIdentifier(), ex);
            }
            oidField.setValue(object.getDigitalObjectIdentifier());

            FileDownloader metsDownloader = new FileDownloader(metsResource);
            metsDownloader.extend(metsButton);

            FileDownloader dcDownloader = new FileDownloader(dcResource);
            dcDownloader.extend(dcButton);

            FileDownloader dataDownloader = new FileDownloader(dataResource);
            dataDownloader.extend(dataButton);

            oidField.setSizeFull();
            searchButton.setWidth("128px");

            searchButton.addClickListener((Button.ClickEvent event) -> {
                String oid = oidField.getValue();
                Page.getCurrent().setLocation(URI.create(Page.getCurrent().getLocation().toString() + "?landing&oid=" + oid).toString());
            });

            dcButton.setWidth("128px");
            metsButton.setWidth("128px");
            dataButton.setWidth("128px");

            metadataDownloadButtons.setComponentAlignment(dcButton, Alignment.TOP_LEFT);
            metadataDownloadButtons.setComponentAlignment(metsButton, Alignment.TOP_LEFT);

            //build layout
            Label oidLabel = new Label("<u>Object Id</u>", ContentMode.HTML);
            builder.fillRow(oidLabel, 0, 0, 1);
            oidLabel.addStyleName("myboldcaption");
            builder.addComponent(oidField, 0, 1, 2, 1).addComponent(searchButton, 2, 1);
            Label dcMetadataLabel = new Label("<u>DC Metadata</u>", ContentMode.HTML);
            builder.fillRow(dcMetadataLabel, 0, 2, 1);
            dcMetadataLabel.addStyleName("myboldcaption");

            Set<Map.Entry<String, String>> entries = dcElementMap.entrySet();

            Table dcTable = new Table();
            dcTable.setPageLength(entries.size() + 1);
            dcTable.addContainerProperty("dc:key", String.class, "-");
            dcTable.addContainerProperty("dc:value", String.class, "-");
            entries.forEach((entry) -> {
                Object newItemId = dcTable.addItem();
                Item row1 = dcTable.getItem(newItemId);
                row1.getItemProperty("dc:key").setValue(entry.getKey());
                row1.getItemProperty("dc:value").setValue(entry.getValue());
            });

            dcTable.setWidth("640px");
            dcTable.addStyleName("myboldcaption");
            builder.addComponent(dcTable, 0, 3, 2, 1);
            builder.addComponent(metadataDownloadButtons, 2, 3, 1, 1);

            builder.fillRow(new Label("<u>Data Access</u>", ContentMode.HTML), 0, 4, 1);
            long bytes = DataOrganizationUtils.getAssociatedDataSize(object.getDigitalObjectId());
            String formatted = AbstractFile.formatSize(bytes);
            Label oidDownloadLabel = new Label(object.getDigitalObjectIdentifier() + ".zip (approx. " + formatted + ")");
            oidDownloadLabel.addStyleName("myboldcaption");
            builder.addComponent(oidDownloadLabel, 0, 5, 2, 1);
            builder.addComponent(dataButton, 2, 5, 1, 1);

            mainLayout = builder.getLayout();
            mainLayout.setRowExpandRatio(0, .1f);
            mainLayout.setRowExpandRatio(1, .1f);
            mainLayout.setRowExpandRatio(2, .6f);
            mainLayout.setRowExpandRatio(3, .1f);
            mainLayout.setRowExpandRatio(4, .1f);

            mainLayout.setColumnExpandRatio(0, .1f);
            mainLayout.setColumnExpandRatio(1, .8f);
            mainLayout.setColumnExpandRatio(2, .1f);
        }
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setStyleName("landing");
        HorizontalLayout hLayout = new HorizontalLayout(mainLayout);
        hLayout.setSizeFull();
        hLayout.setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
        setCompositionRoot(hLayout);
    }

    private boolean accessGranted(DigitalObject object, IAuthorizationContext ctx) {
        String objectId = object.getDigitalObjectIdentifier();

        LOGGER.debug("Trying to get public content for digital object id with id {}", new Object[]{objectId});
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Checking access for public context.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            object = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{objectId}, DigitalObject.class);
            if (object == null) {
                LOGGER.error("No published digital object for object id " + objectId + " found.");
                return false;
            }

            //check if single user grants are supported
            if (!ResourceServiceLocal.getSingleton().grantsAllowed(object.getSecurableResourceId(), AuthorizationContext.factorySystemContext())) {
                //no single user grants allowed, check group permission
                try {
                    Role groupRole = (Role) ResourceServiceLocal.getSingleton().getReferenceRestriction(new ReferenceId(object.getSecurableResourceId(), ctx.getGroupId()), AuthorizationContext.factorySystemContext());
                    if (groupRole.atLeast(Role.GUEST)) {
                        return true;
                    }
                    throw new UnauthorizedAccessAttemptException("Public access not available. Granted group role < GUEST.");
                } catch (EntityNotFoundException ex) {
                    //not for this group
                    throw new UnauthorizedAccessAttemptException("Public access not available. Access on group level not allowed.");
                }
            }

            //check role for user WORLD
            Role publicRole = ResourceServiceLocal.getSingleton().getGrantRole(object.getSecurableResourceId(), ctx.getUserId(), AuthorizationContext.factorySystemContext());

            if (!publicRole.atLeast(Role.GUEST)) {
                throw new UnauthorizedAccessAttemptException("Public access not available. Granted user role < GUEST.");
            }
            return true;
        } catch (UnauthorizedAccessAttemptException | edu.kit.dama.authorization.exceptions.EntityNotFoundException ex) {
            LOGGER.error("Failed to obtain object for object id " + objectId + ". Object seems not to be publicly available.", ex);
            return false;
        } finally {
            mdm.close();
        }
    }

}
