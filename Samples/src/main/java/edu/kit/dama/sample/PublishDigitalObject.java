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
package edu.kit.dama.sample;

import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.rest.dataorganization.client.impl.DataOrganizationRestClient;
import edu.kit.dama.rest.sharing.client.impl.SharingRestClient;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.IOException;

/**
 * A special case of sharing a digital object is making it publicly available by
 * publishing. The basic workflow is the same as if it is shared with a single
 * user or group despite the fact, that publishing has some additional
 * assumptions and border conditions.
 *
 * For the publishing use case KIT Data Manager offers a special user with the
 * user id 'WORLD', which is created at installation time. Publishing a digital
 * object is simply achieved by granting GUEST access to the object for the user
 * WORLD. Afterwards, this object is accessible via the repository landing page
 * without authentication.
 *
 * <b>Attention:</b> The typical way to access published digital objects is via
 * the landing page. However, when accessing partricular RESTful web service
 * without credentials, the access will also be authorized as user 'WORLD'.
 *
 * Accessing the data of a published object is done easily via the
 * DataOrganization service when calling GET /organization/public/{objectId}
 *
 * As just mentioned, the data of the published object can be obtained either
 * via the langing page or via the RESTful web service. During publication it is
 * highly recommended to create a new DataOrganization view named 'public' and
 * to place only elements in there that should really be publicly available. If
 * no such view exists, the view with the name 'default' will be delivered when
 * requesting the data of a published object.
 *
 * @author jejkal
 */
public class PublishDigitalObject extends BasicDataIngest {

    public void publishDigitalObject() throws IOException {

        //perform the base metadata creation and ingest from the super class in order to have a digital object.
        createBaseMetadata();
        ingestData();

        //create view
        //Publishing the digital object is done in the same way as sharing, so we'll need the sharing client.
        SharingRestClient sharingClient = new SharingRestClient(restBaseUrl + "/rest/sharing/", context);
        //First, we obtain the securable resource id.
        SecurableResourceId resourceId = newDigitalObject.getSecurableResourceId();

        //As mentioned before, publishing is basically just granting access to the user  with the userId 'WORLD', so let's do so.
        GrantWrapper grantWrapper = sharingClient.createGrant(resourceId.getDomain(), resourceId.getDomainUniqueId(), Constants.WORLD_USER_ID, Constants.WORLD_GROUP_ID, Role.GUEST, context);
        Grant grant = grantWrapper.getEntities().get(0);
        System.out.println("Successfully created grant with id " + grant.getId() + " and role " + grant.getGrantedRole());
        System.out.println("DigitalObject with id " + newDigitalObject.getDigitalObjectIdentifier() + " is now published.\n"
                + "You may want to access it via its landing page available at http://localhost:8080/KITDM?landing&oid=" + newDigitalObject.getDigitalObjectIdentifier() + ".\n"
                + "Attention: Protocol, hostname and port of the landing page URL might be different for custom repository installations.");

        //Apart from testing the publication via accessing the landing page, we can also try to download the content via the data organization service.
        DataOrganizationRestClient doClient = new DataOrganizationRestClient(restBaseUrl + "/rest/dataorganization/", context);
        //Now, let's try to download the published data. 
        doClient.downloadPublishedData(newDigitalObject.getDigitalObjectIdentifier(), new File("."), "download.zip");
        //Success, we're finished.
    }
}
