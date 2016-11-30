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
package edu.kit.dama.mdm.content.util;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author jejkal
 */
public class DublinCoreHelper {

    /**
     * Create the Dublin Core document.
     *
     * @param theObject The object to create the DC information for.
     * @param pCreator A custom creator stored as author/publisher in Dublin
     * Core. If not provided, the object's uploader is used if available.
     *
     * @return The Dublin Core Document.
     *
     * @throws ParserConfigurationException If creating the Dublin Core document
     * failed.
     */
    public static Document createDublinCoreDocument(DigitalObject theObject, UserData pCreator) throws ParserConfigurationException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");
        root.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");

        doc.appendChild(root);

        Element title = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:title");
        title.setTextContent(StringEscapeUtils.escapeXml11(theObject.getLabel()));
        root.appendChild(title);

        if (pCreator != null) {
            Element creator = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:creator");
            Element publisher = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:publisher");
            creator.setTextContent(StringEscapeUtils.escapeXml11(pCreator.getFullname()));
            publisher.setTextContent(StringEscapeUtils.escapeXml11(pCreator.getFullname()));
            root.appendChild(creator);
            root.appendChild(publisher);
        } else if (theObject.getUploader() != null) {
            Element creator = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:creator");
            Element publisher = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:publisher");
            creator.setTextContent(StringEscapeUtils.escapeXml11(theObject.getUploader().getFullname()));
            publisher.setTextContent(StringEscapeUtils.escapeXml11(theObject.getUploader().getFullname()));
            root.appendChild(creator);
            root.appendChild(publisher);
        }

        for (UserData experimenter : theObject.getExperimenters()) {
            //don't list uploader a second time here
            if (theObject.getUploader() == null || !experimenter.equals(theObject.getUploader())) {
                Element contributor = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:contributor");
                contributor.setTextContent(StringEscapeUtils.escapeXml11(experimenter.getFullname()));
                root.appendChild(contributor);
            }
        }

        if (theObject.getInvestigation() != null) {
            Element subject = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:subject");
            subject.setTextContent(StringEscapeUtils.escapeXml11(theObject.getInvestigation().getTopic()));
            root.appendChild(subject);
            if (theObject.getInvestigation().getDescription() != null) {
                Element description = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:description");
                description.setTextContent(StringEscapeUtils.escapeXml11(theObject.getInvestigation().getDescription()));
                root.appendChild(description);
            }

            if (theObject.getInvestigation().getStudy() != null) {
                if (theObject.getInvestigation().getStudy().getLegalNote() != null) {
                    Element rights = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:rights");
                    rights.setTextContent(StringEscapeUtils.escapeXml11(theObject.getInvestigation().getStudy().getLegalNote()));
                    root.appendChild(rights);
                }
            }
        }

        if (theObject.getStartDate() != null) {
            Element date = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:date");
            date.setTextContent(df.format(theObject.getStartDate()));
            root.appendChild(date);
        }
        Element format = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:format");
        format.setTextContent("application/octet-stream");
        root.appendChild(format);
        Element type = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:type");
        type.setTextContent("Dataset");
        root.appendChild(type);
        Element identifier = doc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:identifier");
        identifier.setTextContent(StringEscapeUtils.escapeXml11(theObject.getDigitalObjectId().getStringRepresentation()));
        root.appendChild(identifier);
        return doc;
    }

    /**
     * Create the Dublin Core document.
     *
     * @param theObject The object to create the DC information for.
     * @param pCreator A custom creator stored as author/publisher in Dublin
     * Core. If not provided, the object's uploader is used if available.
     * @param out The output stream to which the DC document is written.
     *
     * @throws ParserConfigurationException If creating the Dublin Core document
     * failed.
     */
    public static void writeDublinCoreDocument(DigitalObject theObject, UserData pCreator, OutputStream out) throws ParserConfigurationException {
        Document doc = createDublinCoreDocument(theObject, pCreator);
        DOMImplementation impl = doc.getImplementation();
        DOMImplementationLS implLS = (DOMImplementationLS) impl.getFeature("LS", "3.0");

        LSOutput lso = implLS.createLSOutput();
        lso.setByteStream(out);
        LSSerializer writer = implLS.createLSSerializer();
        writer.write(doc, lso);
    }

    /**
     * Create the Dublin Core element map.
     *
     * @param theObject The object to create the DC information for.
     * @param pCreator A custom creator stored as author/publisher in Dublin
     * Core. If not provided, the object's uploader is used if available.
     *
     * @return The Dublin Core elements as map.
     *
     * @throws ParserConfigurationException If creating the Dublin Core document
     * failed.
     */
    public static Map<String, String> createDublinCoreElementMap(DigitalObject theObject, UserData pCreator) throws ParserConfigurationException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Map<String, String> elements = new LinkedHashMap<>();

        elements.put("dc:title", StringEscapeUtils.escapeHtml4(theObject.getLabel()));

        if (pCreator != null) {
            elements.put("dc:creator", StringEscapeUtils.escapeHtml4(pCreator.getFullname()));
            elements.put("dc:publisher", StringEscapeUtils.escapeHtml4(pCreator.getFullname()));
        } else if (theObject.getUploader() != null) {
            elements.put("dc:creator", StringEscapeUtils.escapeHtml4(theObject.getUploader().getFullname()));
            elements.put("dc:publisher", StringEscapeUtils.escapeHtml4(theObject.getUploader().getFullname()));
        }

        theObject.getExperimenters().stream().filter((experimenter) -> (theObject.getUploader() == null || !experimenter.equals(theObject.getUploader()))).forEach((experimenter) -> {
            elements.put("dc:contributor", StringEscapeUtils.escapeHtml4(experimenter.getFullname()));
        }); //don't list uploader a second time here

        if (theObject.getInvestigation() != null) {
            elements.put("dc:subject", StringEscapeUtils.escapeHtml4(theObject.getInvestigation().getTopic()));
            if (theObject.getInvestigation().getDescription() != null) {
                elements.put("dc:description", StringEscapeUtils.escapeHtml4(theObject.getInvestigation().getDescription()));
            }

            if (theObject.getInvestigation().getStudy() != null) {
                if (theObject.getInvestigation().getStudy().getLegalNote() != null) {
                    elements.put("dc:rights", StringEscapeUtils.escapeHtml4(theObject.getInvestigation().getStudy().getLegalNote()));
                }
            }
        }

        if (theObject.getStartDate() != null) {
            elements.put("dc:date", df.format(theObject.getStartDate()));
        }
        elements.put("dc:format", "application/octet-stream");
        elements.put("dc:type", "Dataset");
        elements.put("dc:identifier", StringEscapeUtils.escapeHtml4(theObject.getDigitalObjectId().getStringRepresentation()));
        return elements;
    }
}
