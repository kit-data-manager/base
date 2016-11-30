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
package edu.kit.dama.util.test;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.content.mets.util.MetsBuilder;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;

/**
 *
 * @author jejkal
 */
public class MetsTest {

    public static void main(String[] args) throws Exception {
//        PodamFactory FACTORY = new PodamFactoryImpl(new CustomPodamProviderStrategy());
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            DigitalObject theObject = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{"3852284e-17d8-4bc2-a067-d75692a7c94e"}, DigitalObject.class);
//        DigitalObject theObject = FACTORY.manufacturePojo(DigitalObject.class);
//        theObject.setDigitalObjectId(new DigitalObjectId("3852284e-17d8-4bc2-a067-d75692a7c94e"));
//        theObject.setBaseId(58l);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            MetsBuilder.init(theObject).createMinimalMetsDocument(UserData.WORLD_USER).write(bout);

            System.out.println(bout.toString());

            URL metsSchema = new URL("http://www.loc.gov/standards/mets/mets.xsd");
            Source schemaFile = new StreamSource(metsSchema.openStream());
            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware(true);
            DocumentBuilder parser = fac.newDocumentBuilder();
            Document doc = parser.parse(new ByteArrayInputStream(bout.toByteArray()));

            Validator validator = schema.newValidator();
            System.out.println("Validating....");
            validator.validate(new DOMSource(doc));
            System.out.println("Validation succeeded.");
        } finally {
            mdm.close();

        }
    }
}
